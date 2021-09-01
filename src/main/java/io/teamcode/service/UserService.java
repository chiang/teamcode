package io.teamcode.service;

import io.teamcode.TeamcodeException;
import io.teamcode.common.security.HashingDigester;
import io.teamcode.common.security.HtPasswdVariant;
import io.teamcode.common.security.svn.AuthzException;
import io.teamcode.common.vcs.svn.SvnSecurityHelper;
import io.teamcode.common.vcs.svn.SvnUtils;
import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.*;
import io.teamcode.dto.UserListView;
import io.teamcode.repository.ProjectMemberRepository;
import io.teamcode.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by chiang on 2017. 1. 19..
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    TcConfig tcConfig;

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    ProjectService projectService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    public long countAll() {

        return userRepository.count();
    }

    @Transactional
    public User newUser(User user) {

        if (user.getUserRole() == null)
            user.setUserRole(UserRole.ROLE_USER);

        //패스워드가 없는 사용자도 있을 수 있다. 즉 여기 시스템의 사용자가 아닌 파트너 구성원인 경우.
        if (StringUtils.hasText(user.getPassword())) {
            String originalPassword = new String(user.getPassword().getBytes());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            //user.setPassword(HashingDigester.generateEncryptedPassword(tcConfig.getSubversionRealm(), user.getName(), user.getPassword(), HtPasswdVariant.ALG_APMD5));

            SvnSecurityHelper.addOrUpdateUser(tcConfig.getSubversionRealm(), tcConfig.getPasswordFile().getAbsolutePath(), user.getName(), originalPassword, true);
        }

        if (!StringUtils.hasText(user.getNotificationEmail()))
            user.setNotificationEmail(user.getEmail());

        User savedUser = userRepository.save(user);



        return savedUser;
    }


    /**
     *
     * @param name 사용자 아이디 (name)이 바뀔 수도 있다. 그래서 name 을 받아서 사용자를 찾는 용도로 이 파리미터가 필요하다.
     * @param user
     * @param passwordConfirmation <code>null</code> 이면 비밀번호 변경 요청이 아닌 것입니다.
     * @return
     */
    @Transactional
    public User update(final String name, User user, final String passwordConfirmation) {
        User currentUser = getCurrentUser();

        User existUser = get(name);

        //FIXME user.name 을 수정하게 하는 것이 적합한가?

        existUser.setFullName(user.getFullName());
        existUser.setEmail(user.getEmail());//FIXME 이미 Notification Email 로 설정되어 있다면 어떻게 하는가?
        existUser.setOrganization(user.getOrganization());
        existUser.setUpdatedAt(new Date());

        if (currentUser.getId().longValue() != existUser.getId().longValue()) {
            if (user.isAdmin()) {
                existUser.setUserRole(UserRole.ROLE_ADMIN);
            } else {
                existUser.setUserRole(UserRole.ROLE_USER);
            }
        }
        else {
            logger.warn("자기 자신이 관리자인 경우 자신의 역할을 변경할 수 없습니다.");
        }

        if (StringUtils.hasText(passwordConfirmation) && StringUtils.hasText(user.getPassword())) {
            validatePassword(user, passwordConfirmation);

            logger.debug("사용자 '{}' 정보 업데이트 시 비밀번호도 함께 업데이트 요청을 받았습니다. 비밀번호도 함께 업데이트합니다.", existUser.getName());
            if (passwordConfirmation.equals(user.getPassword())) {
                existUser.setPassword(passwordEncoder.encode(user.getPassword()));
                //existUser.setPassword(HashingDigester.generateEncryptedPassword(tcConfig.getSubversionRealm(), existUser.getName(), user.getPassword(), HtPasswdVariant.ALG_APMD5));
                SvnSecurityHelper.addOrUpdateUser(tcConfig.getSubversionRealm(), tcConfig.getPasswordFile().getAbsolutePath(), user.getName(), user.getPassword(), true);
            }
            else {
                //TODO custom exception name
                throw new InvalidCurrentPasswordException("입력한 두 비밀번호가 일치하지 않습니다.");
            }
        }
        //관리자인 경우 FIXME 잘 분리해야겠다.
        else if (!StringUtils.hasText(passwordConfirmation) && StringUtils.hasText(user.getPassword())) {
            validatePassword(user, passwordConfirmation);

            logger.debug("관리자가 사용자 '{}' 정보 업데이트 시 비밀번호도 함께 업데이트 요청을 받았습니다. 비밀번호도 함께 업데이트합니다.", existUser.getName());
            //TODO 디비의 패스워드와 서브버전의 패스워드를 다른 것을 사용해도 되나?
            existUser.setPassword(passwordEncoder.encode(user.getPassword()));
            //existUser.setPassword(HashingDigester.generateEncryptedPassword(tcConfig.getSubversionRealm(), existUser.getName(), user.getPassword(), HtPasswdVariant.ALG_APMD5));
            SvnSecurityHelper.addOrUpdateUser(tcConfig.getSubversionRealm(), tcConfig.getPasswordFile().getAbsolutePath(), user.getName(), user.getPassword(), true);
        }

        existUser.setUpdatedBy(currentUser);

        User updatedUser = userRepository.save(existUser);
        return updatedUser;
    }

    private boolean validatePassword(final User currentUser, final String password) {
        Assert.notNull(password);

        if (password.trim().length() < 8) {
            throw new IllegalArgumentException("비밀번호를 8자리 이상으로 입력해 주세요.");
        }

        if (currentUser.getPassword().equals(password)) {
            throw new IllegalArgumentException("직전 비밀번호를 새 비밀번호로 사용할 수 없습니다.");
        }

        if (!password.matches("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$")) {
            throw new IllegalArgumentException("비밀번호는 영문, 숫자, 특수문자 조합이어야 하며, 대문자와 소문자를 최소 하나씩 포함해야 합니다.");
        }

        return true;
    }

    @Transactional
    public User patch(final User userForm) {
        User exist = userRepository.findOne(userForm.getId());

        if(StringUtils.hasText(userForm.getNotificationEmail()))
            exist.setNotificationEmail(userForm.getNotificationEmail());

        if(userForm.getTheme() != null)
            exist.setTheme(userForm.getTheme());

        if(userForm.getLayout() != null)
            exist.setLayout(userForm.getLayout());

        exist.setUpdatedBy(getCurrentUser());
        exist.setUpdatedAt(new Date());

        User patched = userRepository.save(exist);
        logger.debug("사용자 '{}' 의 정보를 패치했습니다.", patched.getName());

        return patched;
    }

    /**
     * Current User 가 없을 수는 없다. 왜냐면 로그인을 하지 않았다면 이 코드까지 호출하지도 못할테니까.
     * 이 정보는 세션에 있는 사용자 아이디를 기반으로 데이터베이스에 저장된 정보를 가져옵니다.
     *
     * @return Entity
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new IllegalStateException("not login");//TODO custom excetion
        }
        //TODO check anonymous


        String name = auth.getName(); //getDto logged in username
        User currentUser = get(name);

        return currentUser;
    }

    /**
     * 세션 정보를 조회합니다. getCurrentUser 와는 달리 이 정보는 메모리에 저장되어 있습니다.
     *
     * @return
     */
    public User getSessionUser() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();

        if (attr.getRequest().getSession(false) != null)
            return (User)attr.getRequest().getSession(false).getAttribute("currentUser");
        else
            return null;
    }

    public ProjectRole getUserProjectRole(String projectPath, String userName) {
        User user = get(userName);
        ProjectMember projectMember = projectMemberRepository.findByProjectAndUser(projectService.getByPath(projectPath), user);
        if (projectMember == null)
            return null;
        else
            return projectMember.getRole();
    }

    public ProjectRole getCurrentUserProjectRole(final String projectPath) {
        User user = getSessionUser();

        ProjectMember projectMember = projectMemberRepository.findByProjectAndUser(projectService.getByPath(projectPath), user);
        if (projectMember == null)
            return null;
        else
            return projectMember.getRole();
    }

    public User get(final Long userId) {
        //TODO Assertion

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException(String.format("사용자 '#%s' 를 찾을 수 없습니다.", userId));
        }

        return user;
    }

    public User get(final String userName) {
        //TODO Assertion

        User user = userRepository.findByName(userName);
        if (user == null) {
            throw new ResourceNotFoundException(String.format("사용자 '%s' 를 찾을 수 없습니다.", userName));
        }

        return user;
    }

    public List<User> getAdminUsers(Sort sort) {

        return userRepository.findByUserRole(UserRole.ROLE_ADMIN, sort);
    }

    public long countByState(UserState userState) {

        return userRepository.countByState(userState);
    }

    public List<User> getActiveUsers(Sort sort) {

        return userRepository.findByState(UserState.ACTIVE, sort);
    }

    public List<User> getBlockedUsers(Sort sort) {

        return userRepository.findByState(UserState.BLOCKED, sort);
    }

    public UserListView getUserListView(final String filter, final Sort sort) {
        UserListView userListView = new UserListView();
        userListView.setFilter(filter);
        userListView.setActiveUsersCount(userRepository.countByState(UserState.ACTIVE));
        userListView.setBlockedUsersCount(userRepository.countByState(UserState.BLOCKED));
        userListView.setAdminsCount(userRepository.countByUserRole(UserRole.ROLE_ADMIN));

        if ("blocked".equals(filter))
            userListView.setUsers(getBlockedUsers(sort));
        else if ("admins".equals(filter))
            userListView.setUsers(getAdminUsers(sort));
        else
            userListView.setUsers(getActiveUsers(sort));

        User currentUser = getCurrentUser();
        if (currentUser != null)
            userListView.getUsers().stream().forEach(u -> {
                if (u.getName().equals(currentUser.getName()))
                    u.setMe(true);
            });

        return userListView;
    }

    @Transactional
    public void block(final String userName) {
        User currentUser = getCurrentUser();
        if (currentUser.getName().equals(userName)) {
            logger.warn("자기 자신은 차단할 수 없습니다.");
        }
        else {
            User user = get(userName);
            user.setState(UserState.BLOCKED);
            user.setUpdatedAt(new Date());

            userRepository.save(user);

            boolean enabled = false;
            if (enabled) {
                SvnSecurityHelper.blockUser(tcConfig.getAuthz(), user.getName());
                logger.debug("Authz 파일에서 이 사용자에 대한 모든 권한을 Deny 로 설정했습니다.");
            }

            logger.info("사용자 '{}' (을)를 차단했습니다.", userName);
        }
    }

    @Transactional
    public void unblock(final String userName) {
        User currentUser = getCurrentUser();
        if (currentUser.getName().equals(userName)) {
            logger.warn("자기 자신은 차단 해제할 수 없습니다.");
        }
        else {
            User user = get(userName);
            user.setState(UserState.ACTIVE);
            user.setUpdatedAt(new Date());

            userRepository.save(user);

            List<ProjectMember> projectMembers = projectService.getMembersBysUserName(userName);
            boolean enabled = false;
            if (enabled) {
                try {
                    SvnSecurityHelper.unblockUser(tcConfig.getAuthz(), user.getName(), projectMembers.toArray(new ProjectMember[projectMembers.size()]));
                    logger.debug("Authz 파일에서 이 사용자에 대한 모든 권한을 원 상태로 복구했습니다.");
                } catch (AuthzException e) {
                    //TODO custom
                    throw new TeamcodeException(e);
                } catch (IOException e) {
                    //TODO custom
                    throw new TeamcodeException(e);
                }
            }
            logger.info("사용자 '{}' (을)를 차단 해제했습니다.", userName);
        }
    }
}
