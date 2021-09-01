package io.teamcode.common.vcs.svn;

import io.teamcode.common.security.HashingDigester;
import io.teamcode.common.security.svn.*;
import io.teamcode.TeamcodeSecurityException;
import io.teamcode.domain.entity.ProjectMember;
import io.teamcode.domain.entity.ProjectRole;
import io.teamcode.util.DateUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.List;

/**
 * Apache 의 htdigest 에 대응하는 기능을 가지고 있습니다.
 *
 * Created by chiang on 2017. 2. 28..
 */
public abstract class SvnSecurityHelper {

    private static final Logger logger = LoggerFactory.getLogger(SvnSecurityHelper.class);

    /**
     *
     * @param userId
     * @param password
     * @param isForce <code>true</code>이면 user가 없어도 추가한 후 생성한다.
     */
    public final static void addOrUpdateUser(final String realm, final String path, String userId, String password, boolean isForce) {
        logger.info("updating user '{}' of subversion...", userId);

        if (!(StringUtils.hasText(userId) && StringUtils.hasText(password))) {
            throw new TeamcodeSecurityException("user id or password is empty.");
        }

        try {
            File passwordFile = Paths.get(path).toFile();
            List<String> users = FileUtils.readLines(passwordFile, Charset.defaultCharset());
            int indexToUpdate = -1;
            String userLine = null;
            for (int i = 0; i < users.size(); i++) {
                userLine = users.get(i);
                if (userLine.startsWith(userId + ":")) {
                    indexToUpdate = i;
                    break;
                }
            }

            if (indexToUpdate < 0 && !isForce) {
                throw new TeamcodeSecurityException("cannot update user. cannot find user '" + userId + "'.");
            }
            else if (indexToUpdate < 0 && isForce) {
                users.add(HashingDigester.generateHtDigest(userId, password, realm));
                syncPasswordFile(users, passwordFile);
                logger.info("user '{}' password is updated (forced).", userId);
            }
            else {
                users.remove(indexToUpdate);
                users.add(HashingDigester.generateHtDigest(userId, password, realm));

                syncPasswordFile(users, passwordFile);
                logger.info("user '{}' password is updated.", userId);
            }
        } catch (IOException e) {
            throw new TeamcodeSecurityException("cannot udpate user of subversion.", e);
        }
    }

    public final static void deleteUser(final String path, String userId) {
        logger.info("deleting user '{}' of subversion...", userId);

        if (!StringUtils.hasText(userId)) {
            throw new TeamcodeSecurityException("user id is empty.");
        }

        try {
            File passwordFile = Paths.get(path).toFile();
            List<String> users = FileUtils.readLines(passwordFile, Charset.defaultCharset());
            int indexToDelete = -1;
            String userLine = null;
            for (int i = 0; i < users.size(); i++) {
                userLine = users.get(i);
                if (userLine.startsWith(userId + ":")) {
                    indexToDelete = i;
                    break;
                }
            }

            if (indexToDelete < 0) {
                throw new TeamcodeSecurityException("cannot delete user. cannot find user '" + userId + "'.");
            }
            else {
                users.remove(indexToDelete);

                syncPasswordFile(users, passwordFile);
                logger.info("user '{}' is deleted.", userId);
            }
        } catch (IOException e) {
            throw new TeamcodeSecurityException("cannot delete user of subversion.", e);
        }
    }

    private final static void syncPasswordFile(List<String> users, final File passwordFile) throws IOException {
        FileUtils.copyFile(passwordFile, new File(passwordFile.getParentFile(), passwordFile.getName() + String.format(".old.%s", DateUtils.defaultBackupSuffix())));
        FileUtils.writeLines(passwordFile, users);
    }

    private static final void syncAuthzFile(final Authz authz, final File authzFile) throws IOException {
        String newAuthz = new AuthzFileGenerator(authz).generate(true);
        FileUtils.copyFile(authzFile, new File(authzFile.getParentFile(), authzFile.getName() + ".prev"));
        FileUtils.writeStringToFile(authzFile, newAuthz, false);
    }

    public final static void updatePermission(final File authzFile, ProjectMember... projectMembers) throws AuthzException, IOException {
        try {
            AuthzFileParser parser = new AuthzFileParser();
            Authz authz = parser.parse(authzFile);

            for(ProjectMember projectMember: projectMembers) {
                AuthzUser existUser = authz.getUserWithName(projectMember.getUser().getName());
                if (existUser == null) {
                    existUser = authz.createUser(projectMember.getUser().getName(), null);
                }

                AuthzRepository repository = authz.getRepositoryWithName(projectMember.getProject().getPath());
                AuthzPath authzPath = authz.getPath(repository, "/");
                //repository를 생성할 때마다 authz를 구성하지는 않기 때문에 이렇게 넣을 때 체크한다.
                if (repository == null) {
                    repository = authz.createRepository(projectMember.getProject().getPath());
                    authzPath = authz.createPath(repository, "/");
                }

                AuthzAccessRule foundAccessRule = null;
                for (AuthzAccessRule rule : authzPath.getAccessRules()) {
                    if (rule.getPermissionable() instanceof AuthzUser) {
                        if (existUser.getName().equals((rule.getPermissionable()).getName())) {
                            foundAccessRule = rule;
                            break;
                        }
                    }
                }

                if (foundAccessRule != null) {
                    authz.deleteAccessRule(foundAccessRule);
                }
                createAccessRule(authz, existUser, authzPath, projectMember.getRole());
            }

            //FIXME 비교를 해서 변경 사항이 없으면 처리하도록 수정.
            SvnUtils.syncAuthzFile(authz, authzFile);
        } catch (AuthzException e) {
            //logger.error("cannot update user privilege. skipping update privilege", e);
            throw e;
        } catch (IOException e) {
            //logger.error("cannot update user privilege. skipping update privilege", e);
            throw e;
        }
    }

    /**
     * <p>User를 Repository에 접근하지 못하도록 한다. Password 파일에서 제거하는 방식이 아닌
     * Authz 파일에서 Deny 하는 방식을 취한다.</p>
     *
     * FIXME 암호화 알고리즘을 맞추면 관계없지 않을까? SVN Realm 때문에 안되나?
     * <p>패스워드 파일에서 제거를 하면 다시 생성을 하지 못한다. 패스워드는 단방향 Hash이기 때문에
     * 다시 추가하려면 사용자의 패스워드 원본 입력을 받아야 하므로 적절하지 않다.</p>
     *
     *
     * @param authzFile 파일
     * @param userName
     * @return
     */
    public static final Authz blockUser(final File authzFile, final String userName) {
        logger.info("blocking user '{}' ...", userName);
        AuthzFileParser parser = new AuthzFileParser();

        Authz authz;
        try {
            authz = parser.parse(authzFile);

            AuthzUser authzUser = authz.getUserWithName(userName);
            if (authzUser == null) {
                logger.warn("user '{}' not found. skip blocking user..", userName);
                return authz;
            }

            List<AuthzAccessRule> accessRules = authzUser.getAccessRules();

            for (AuthzAccessRule rule: accessRules) {
                authz.deleteAccessRule(rule);
            }

            SvnUtils.syncAuthzFile(authz, authzFile);

            return authz;
        } catch (AuthzException e) {
            throw new TeamcodeSecurityException("cannot block user.", e);
        } catch (IOException e) {
            throw new TeamcodeSecurityException("cannot block user.", e);
        }
    }

    public static final Authz unblockUser(final File authzFile, final String userName, final ProjectMember... projectMembers) throws AuthzException, IOException{
        logger.info("unblocking user '{}' ...", userName);

        AuthzFileParser parser = new AuthzFileParser();

        Authz authz = parser.parse(authzFile);

        //TODO AuthzUser를 못 차는 경우도 RepositoryUserMap 기반으로 처리가 되게끔 해 줄 필요가 있다.
        AuthzUser authzUser = authz.getUserWithName(userName);
        if (authzUser == null) {
            logger.warn("user '{}' not found. skip unblocking user..", userName);
            authzUser = authz.createUser(userName, null);
        }

        AuthzRepository authzRepository = null;
        AuthzPath authzPath = null;

        for(ProjectMember projectMember: projectMembers) {
            authzRepository = authz.getRepositoryWithName(projectMember.getProject().getPath());
            authzPath = authz.getPath(authzRepository, "/");

            //repository를 생성할 때마다 authz를 구성하지는 않기 때문에 이렇게 넣을 때 체크한다.
            if (authzRepository == null) {
                authzRepository = authz.createRepository(projectMember.getProject().getPath());
                authzPath = authz.createPath(authzRepository, "/");
            }

            createAccessRule(authz, authzUser, authzPath, projectMember.getRole());
        }

        SvnUtils.syncAuthzFile(authz, authzFile);

        return authz;
    }

    //TODO naming
    public static final Authz deleteToRepository(final File authzFile, ProjectMember projectMember) {
        AuthzFileParser parser = new AuthzFileParser();

        Authz authz = null;
        AuthzPath authzPath = null;
        try {
            authz = parser.parse(authzFile);
            AuthzRepository repository = authz.getRepositoryWithName(projectMember.getProject().getPath());
            authzPath = authz.getPath(repository, "/");

            //그냥 무시.
            if (repository == null) {
                logger.warn("repository '{}' not found. skipping delete user '{}'...", projectMember.getProject().getPath(), projectMember.getUser().getName());
                return authz;
            }

            //그냥 무시
            AuthzUser authzUser = authz.getUserWithName(projectMember.getUser().getName());
            if (authzUser == null) {
                logger.warn("user '{}' not found. skipping delete user '{}'...", projectMember.getUser().getName(), projectMember.getUser().getName());
                return authz;
            }

            AuthzAccessRule foundAccessRule = null;
            for (AuthzAccessRule rule: authzPath.getAccessRules()) {
                if (rule.getPermissionable() instanceof AuthzUser) {
                    if (authzUser.getName().equals((rule.getPermissionable()).getName())) {
                        foundAccessRule = rule;
                        break;
                    }
                }
            }

            if (foundAccessRule != null) {
                authz.deleteAccessRule(foundAccessRule);
            }
            //authz.deleteUser(authzUser, repositoryName);

            SvnUtils.syncAuthzFile(authz, authzFile);

            return authz;
        } catch (AuthzException e) {
            throw new TeamcodeSecurityException("cannot add user to repository.", e);
        } catch (IOException e) {
            throw new TeamcodeSecurityException("cannot add user to repository.", e);
        }
    }

    public static final void deleteRepository(final File authzFile, final String repositoryName) {
        AuthzFileParser parser = new AuthzFileParser();

        try {
            Authz authz = parser.parse(authzFile);
            AuthzRepository repository = authz.getRepositoryWithName(repositoryName);

            if (repository == null) {
                logger.warn("repository '{}' not found. skipping delete repository '{}'...", repositoryName, repositoryName);
            }
            else {
                AuthzPath authzPath = authz.getPath(repository, "/");
                authz.deletePath(authzPath);

                SvnUtils.syncAuthzFile(authz, authzFile);
            }

            logger.info("Authz 파일에서 저장소 '{}' 를 삭제했습니다.", repositoryName);
        } catch (AuthzException e) {
            throw new TeamcodeSecurityException("cannot add user to repository.", e);
        } catch (IOException e) {
            throw new TeamcodeSecurityException("cannot add user to repository.", e);
        }
    }

    private static final void createAccessRule(final Authz authz, final AuthzUser authzUser, final AuthzPath authzPath, final ProjectRole projectRole) throws AuthzAccessRuleAlreadyExistsException, AuthzAccessRuleAlreadyAppliedException {
        switch (projectRole) {
            case OWNER:
            case MASTER:
            case DEVELOPER:
                authz.createAccessRule(authzPath, authzUser, AuthzPrivilege.READ_WRITE);
                break;

            case REPORTER:
            case GUEST:
                authz.createAccessRule(authzPath, authzUser, AuthzPrivilege.READ_ONLY);
                break;

            default:
                authz.createAccessRule(authzPath, authzUser, AuthzPrivilege.DENY_ACCESS);
                break;
        }
    }

}
