package io.teamcode.service;

import io.teamcode.TeamcodeException;
import io.teamcode.TeamcodeSecurityException;
import io.teamcode.common.TeamcodeConstants;
import io.teamcode.common.security.svn.AuthzException;
import io.teamcode.common.vcs.RepositoryHelper;
import io.teamcode.common.vcs.svn.*;
import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.*;
import io.teamcode.repository.ProjectLinkRepository;
import io.teamcode.repository.ProjectMemberRepository;
import io.teamcode.repository.ProjectRepository;
import io.teamcode.service.vcs.svn.CommitService;
import io.teamcode.util.DateUtils;
import io.teamcode.util.StorageUtils;
import io.teamcode.web.ui.view.ProjectOverviewView;
import org.apache.commons.io.FilenameUtils;
import org.apache.subversion.javahl.types.Revision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by chiang on 2017. 2. 6..
 */
@Service
@Transactional(readOnly = true)
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);

    @Autowired
    TcConfig tcConfig;

    @Autowired
    UserService userService;

    @Autowired
    GroupService groupService;

    @Autowired
    EventService eventService;

    @Autowired
    CommitService commitService;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    @Autowired
    ProjectLinkRepository projectLinkRepository;

    public long countAll() {

        return projectRepository.count();
    }

    public List<Project> getArchived() {
        return projectRepository.findByArchived(Boolean.TRUE);
    }

    public List<Project> getNonArchived() {

        List<Project> projects = projectRepository.findByArchived(Boolean.FALSE);

        return projects.stream().filter(p -> !p.getArchived().booleanValue()).collect(Collectors.toList());
    }

    public List<Project> getAllProjects(final String projectName, final Sort sort) {

        List<Project> projects;
        if (StringUtils.hasText(projectName))
            projects = projectRepository.findByNameContaining(projectName, sort);
        else {
            projects = projectRepository.findAll(sort);
        }

        projects.stream().filter(p -> !p.getArchived().booleanValue()).forEach(p -> {
            p.setLastCommit(commitService.getCommit(p.getPath(), Revision.HEAD));
            List<ProjectMember> projectMembers = getMembers(p.getPath());
            p.setMembers(projectMembers.stream().map(pm -> pm.getUser()).collect(Collectors.toList()));
        });

        return projects.stream().filter(p -> !p.getArchived().booleanValue()).collect(Collectors.toList());
    }

    public List<Project> getMyProjects() {
        User currentUser = userService.getCurrentUser();

        List<Project> mergedProjects;
        if (userService.getCurrentUser().isAdmin()) {
            Iterable<Project> projects = projectRepository.findAll();
            mergedProjects = new ArrayList<>();
            for (Project p: projects) {
                if (!p.getArchived().booleanValue())
                    mergedProjects.add(p);
            }
        }
        else {
            List<ProjectMember> projectMembers = projectMemberRepository.findByUser(currentUser);
            List<Project> projects = projectMembers.stream().map(pm -> pm.getProject()).collect(Collectors.toList());
            mergedProjects = projectRepository.findByVisibility(Visibility.PUBLIC);

            //FIXME 공개된 프로젝트에도 멤버를 설정할 수 있다면 공개된 프로젝트를 따로 조회하는 것은 문제가 있지 않나?
            mergedProjects.addAll(projects);
        }

        mergedProjects.stream().filter(p -> !p.getArchived().booleanValue()).forEach(p -> {
            p.setLastCommit(commitService.getCommit(p.getPath(), Revision.HEAD));
            List<ProjectMember> projectMembers = getMembers(p.getPath());
            p.setMembers(projectMembers.stream().map(pm -> pm.getUser()).collect(Collectors.toList()));
        });

        return mergedProjects.stream().filter(p -> !p.getArchived().booleanValue()).collect(Collectors.toList());
    }

    /*@Transactional
    public Project create(Project project) {
        User currentUser = userService.getCurrentUser();

        //TODO 환경 설정을 제어합시다.
        if (currentUser.getUserRole() != UserRole.ROLE_ADMIN)
            throw new InsufficientPrivilegeException("관리자만 프로젝트를 생성할 수 있습니다.");

        if (!StringUtils.hasText(project.getPath()))
            project.setPath(project.getName());
        project.setCreator(currentUser);
        project.setCreatedAt(new Date());
        project.setLastActivityAt(new Date());

        Project savedProject = projectRepository.save(project);
        logger.info("사용자 '{}' 가 새로운 프로젝트 '{}' 를 생성했습니다.", currentUser.getName(), project.getName());

        return savedProject;
    }*/

    /**
     *
     *
     * @param groupPath <code>null</code> 이면 자동으로 기본 프로젝트 group 을 사용합니다.
     * @param project
     * @return
     */
    @Transactional
    public Project create(final String groupPath, Project project) {
        User currentUser = userService.getCurrentUser();

        //TODO 환경 설정을 제어합시다.
        if (currentUser.getUserRole() != UserRole.ROLE_ADMIN)
            throw new InsufficientPrivilegeException("관리자만 프로젝트를 생성할 수 있습니다.");

        if (!StringUtils.hasText(project.getName()))
            project.setName(project.getPath());

        boolean subversion = true;//TODO
        if (subversion) {
            if (projectRepository.findByPath(project.getPath()) != null) {
                throw new AlreadyCreatedException("중복된 프로젝트 이름입니다.");
            }
        }

        project.setCreator(currentUser);
        project.setCreatedAt(new Date());
        project.setLastActivityAt(new Date());
        project.setVisibility(Visibility.INTERNAL);//TODO 설정할 수 있도록?

        if (StringUtils.hasText(groupPath)) {
            project.setGroup(groupService.getByPath(groupPath));
        }
        else {
            project.setGroup(groupService.getByPath("projects"));
        }

        Project savedProject = projectRepository.save(project);
        logger.debug("프로젝트 '{}' 정보를 생성했습니다. 이어서 서브버전 저장소 '{}' 를 생성합니다...", savedProject.getPath(), savedProject.getPath());

        eventService.createProject(savedProject);
        try {
            createRepository(project);
        } catch (IOException e) {
            logger.error("서브버전 저장소 '{}' 를 생성할 수 없습니다.", project.getPath());
            throw new TeamcodeException(e);//TODO custom?
        }

        logger.debug("서브버전 저장소 '{}' 를 생성했습니다.", savedProject.getPath());

        logger.info("사용자 '{}' 가 새로운 프로젝트 '{}' 를 생성했습니다.", currentUser.getName(), project.getName());

        return savedProject;
    }

    private final void modifyRepositoryName(final String originalName, final String renamedName) throws IOException {
        File repositoryDirectory = new File(tcConfig.getRepositoryRootDir(), originalName);
        File renamedDirectory = new File(tcConfig.getRepositoryRootDir(), renamedName);

        if (!repositoryDirectory.renameTo(renamedDirectory)) {
            throw new IOException(String.format("서브버전 저정소 이름을 '%s' 에서 '%s' 로 변경하던 중 오류가 발생했습니다.", originalName, renamedName));
        }

        logger.info("서브버전 저장소 이름을 '{}' 에서 '{}' 으로 변경했습니다.", originalName, renamedName);
    }

    /**
     * 서브버전 저장소를 생성하고 저장소의 UUID 를 반환합니다. 저장소가 이미 생성되었다면 UUID 만 반환합니다.
     *
     * @param project
     */
    private final void createRepository(final Project project) throws IOException {
        File repositoryDirectory = new File(tcConfig.getRepositoryRootDir(), project.getPath());
        if (repositoryDirectory.exists()) {
            logger.warn("이미 서브버전 저장소 디렉터리 '{}' 가 존재합니다. 이 디렉터리가 서브버전 저장소 유형이 아닐 수도 있지만 수동으로 Import 한 저장소일 수도 있습니다. Hook Script 검증만 처리합니다.", project.getPath());

            String repositoryPostCommitHookUrl = RepositoryHelper.buildHookEndpoint(tcConfig.getExternalUrl(), repositoryDirectory.getName(), "");

            logger.debug("'{}' Repository 의 Hook Script 를 검증합니다.", repositoryDirectory.getName());
            logger.debug("'{}' Repository 의 Post Commit Hook URL: <{}>", repositoryDirectory.getName(), repositoryPostCommitHookUrl);

            RepositoryHelper.recoveryIfInvalidPostCommitHook(repositoryDirectory.getName(), repositoryDirectory.getParentFile(), repositoryPostCommitHookUrl, tcConfig.isSyncPermission());
            logger.debug("'{}' 저장소의 pre-revprop-change 스크립트를 동기화합니다...", project.getPath());
            RepositoryHelper.syncPreRevpropChangeScript(repositoryDirectory, tcConfig.isSyncPermission());
        }
        else {
            SvnRepositoryHandler.createRepositoryFromTemplate(tcConfig.getSubversionVersion(), "", tcConfig.getHomeDir(), repositoryDirectory, tcConfig.isSyncPermission());

            String repositoryPostCommitHookUrl = RepositoryHelper.buildHookEndpoint(tcConfig.getExternalUrl(), repositoryDirectory.getName(), "");

            logger.debug("'{}' Repository 의 Hook Script 를 검증합니다.", repositoryDirectory.getName());
            logger.debug("'{}' Repository 의 Post Commit Hook URL: <{}>", repositoryDirectory.getName(), repositoryPostCommitHookUrl);

            RepositoryHelper.recoveryIfInvalidPostCommitHook(repositoryDirectory.getName(), repositoryDirectory.getParentFile(), repositoryPostCommitHookUrl, tcConfig.isSyncPermission());
            logger.debug("'{}' 저장소의 pre-revprop-change 스크립트를 동기화합니다...", project.getPath());
            RepositoryHelper.syncPreRevpropChangeScript(repositoryDirectory, tcConfig.isSyncPermission());
        }
    }

    /**
     *
     * @param path
     * @param project
     * @param multipartFile 이 값은 <code>null</code> 을 허용합니다. <code>null</code> 인 경우는 이미지를 변경하지 않은 경우입니다.
     * @return
     */
    @Transactional
    public Project patch(final String path, Project project, MultipartFile multipartFile) {
        //TODO check owner, master, admin

        Project exist = getByPath(path);

        //프로젝트 이름을 업데이트한다. 만약 name 정보가 같이 안 온다면 이름을 path 로 사용해야 한다.
        if(StringUtils.hasText(project.getName())) {
            exist.setName(project.getName());
        }

        boolean pathUpdated = false;
        String originalName = null;
        if (StringUtils.hasText(project.getPath())) {
            if (!exist.getPath().equals(project.getPath())) {
                originalName = exist.getPath();
                pathUpdated = true;
            }

            exist.setPath(project.getPath());
        }

        if(StringUtils.hasText(project.getDescription()))
            exist.setDescription(project.getDescription());

        if (project.getProgrammingLanguage() != null)
            exist.setProgrammingLanguage(project.getProgrammingLanguage());

        //TODO 나중에 화면 작업하고 나서 활성화
        //exist.setAttachmentsVisibility(project.getAttachmentsVisibility());

        //TODO 만약 이 설정이 Off 되면 현재 돌고 있는 Runner 들은 어찌 되는가?
        //FIXME 기본 값이 DISABLED 인데 이를 Patch 조건에 걸면 null 인 경우가 발생하지 않기 때문에 아래와 같은 조건이 동작하지 않음.
        /*logger.debug("--> pipeline visibility: {}", project.getPipelineVisibility());
        if (project.getPipelineVisibility() != null)
            exist.setPipelineVisibility(project.getPipelineVisibility());*/

        if (StringUtils.hasText(project.getPipelineConfigPath())) {
            exist.setPipelineConfigPath(project.getPipelineConfigPath());
        }

        exist.setUpdatedAt(new Date());

        Project patched = projectRepository.save(exist);

        if (multipartFile != null && !multipartFile.isEmpty()) {
            logger.debug("프로젝트 아바타 파일을 전달받았습니다. 파일 저장을 시작합니다...");
            if (multipartFile.getSize() > TeamcodeConstants.MAX_UPLOAD_AVATAR_SIZE) {
                throw new MaxUploadSizeExceededException(TeamcodeConstants.MAX_UPLOAD_AVATAR_SIZE);
            }

            String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
            String projectDirPath = StorageUtils.buildProjectDirHierarchyStructure(patched.getId());
            File projectDir = new File(tcConfig.getAttachmentsDir(), projectDirPath);

            if (!projectDir.exists()) {
                if (!projectDir.mkdirs())
                    throw new TeamcodeException(String.format("디렉터리를 만들 수 없습니다! %s", projectDir.getAbsolutePath()));
            }

            File avatarFile = new File(projectDir, String.format("avatar.%s", extension));
            try {
                multipartFile.transferTo(avatarFile);

                patched.setAvatarPath(String.format("%s/%s", projectDirPath, avatarFile.getName()));
                projectRepository.save(patched);
            } catch (IOException e) {
                logger.error("프로젝트 아바타 파일을 저장하던 중 오류가 발생했습니다.", e);

                throw new TeamcodeException("프로젝트 아바타 파일을 저장하던 중 오류가 발생했습니다.", e);
            }
        }

        if (pathUpdated) {
            logger.info("프로젝트 '{}' 의 경로가 변경되어 서브버전 경로도 함께 변경합니다...", project.getName());

            try {
                modifyRepositoryName(originalName, patched.getPath());
            } catch (IOException e) {
                logger.error("서브버전 경로를 변경하던 중 오류가 발생했습니다.", e);

                throw new TeamcodeException("서브버전 경로를 변경하던 중 오류가 발생했습니다.", e);
            }
        }

        logger.debug("프로젝트 '{}' 의 정보를 패치했습니다.", patched.getName());

        return patched;
    }

    @Transactional
    public void delete(final String path) {
        Project exist = getByPath(path);
        projectRepository.delete(exist);

        //TODO message?
        logger.info("프로젝트 '{}' 를 삭제했습니다.", exist.getName());
    }

    public Project getByPath(final String path) {
        Project project = projectRepository.findByPath(path);
        if (project == null)
            throw new ResourceNotFoundException(String.format("Project '%s' not found.", path));

        return project;
    }

    public Project get(final String name) {
        //Project project = projectRepository.findByName(name);

        return projectRepository.findByName(name);
    }

    /**
     * 현재 로그인 한 사용자가 해당 프로젝트에 접근할 수 있는지 여부를 반환합니다.
     *
     * @param projectPath
     * @return
     */
    public boolean canAccess(final String projectPath) {
        Project project = getByPath(projectPath);
        if (project.getVisibility() == Visibility.PUBLIC)
            return true;

        User currentUser = userService.getCurrentUser();
        if (currentUser.isAdmin())
            return true;

        return projectMemberRepository.findByProjectAndUser(project, currentUser) != null;
    }

    public ProjectOverviewView getProjectOverviewView(final String projectPath) {
        ProjectOverviewView projectOverviewView = new ProjectOverviewView();
        Project project = this.getByPath(projectPath);//FIXME 하나로 합치기??

        //프로젝트 Overview 화면에 표시할 사용자를 반환합니다. 최대 5명이며, 없는 경우 빈 값을 넣어서 화면에서 제어할 수 있게 합니다.
        User currentUser = userService.getCurrentUser();
        List<ProjectMember> projectMembers = getMembers(projectPath);
        List<User> members = projectMembers.stream()
                .filter(pm -> !pm.getUser().getName().equals(currentUser.getName()))
                .map(pm -> pm.getUser()).collect(Collectors.toList());

        List<User> mergedMembers = new ArrayList<>(5);
        int limit = 5;
        if (projectMembers.stream().anyMatch(pm -> pm.getUser().getName().equals(currentUser.getName()))) {
            mergedMembers.add(userService.getCurrentUser());
            limit = 4;
        }
        if (members.size() > limit) {
            for (int i = (limit == 4 ? 1 : 0); i < 5; i++)
                mergedMembers.add(i, members.get(i));
        }
        else {
            mergedMembers.addAll(members);
            for (int i = mergedMembers.size(); i < 5; i++)
                mergedMembers.add(i, null);
        }

        projectOverviewView.setMembers(mergedMembers);
        projectOverviewView.setTotalMembers(projectMembers.size());
        projectOverviewView.setLinks(getProjectLinks(projectPath));

        if (project.getProgrammingLanguage() != null)
            projectOverviewView.setTags(Arrays.asList(Tag.builder().name(project.getProgrammingLanguage().getDisplayName()).build()));
        else
            projectOverviewView.setTags(Collections.emptyList());

        return projectOverviewView;
    }

    public List<ProjectMember> getMembers(final String projectPath) {
        Project project = getByPath(projectPath);

        User currentUser = userService.getCurrentUser();

        List<ProjectMember> projectMembers = projectMemberRepository.findByProject(project);

        //FIXME 동작하지 않는다.
        Collections.sort(projectMembers, (o1, o2) -> o1.getUser().getId().longValue() == currentUser.getId().longValue() ? 1 : 0);

        return projectMembers;
    }

    public List<ProjectMember> getMembersBysUserName(final String userName) {
        User user = userService.get(userName);

        List<ProjectMember> projectMembers = projectMemberRepository.findByUser(user);

        return projectMembers;
    }

    @Transactional
    public void addUsers(final String projectPath, List<Long> userIds, ProjectRole projectRole) {
        Project project = getByPath(projectPath);
        User user;
        ProjectMember projectMember;

        List<ProjectMember> projectMembers = new ArrayList<>();
        for(Long userId: userIds) {
            user = userService.get(userId);
            projectMember = projectMemberRepository.findByProjectAndUser(project, user);
            if (projectMember == null) {
                projectMember = new ProjectMember();
                projectMember.setProject(project);
                projectMember.setUser(user);
                projectMember.setCreatedAt(new Date());
            }

            projectMember.setRole(projectRole);
            projectMember.setUpdatedAt(new Date());
            projectMembers.add(projectMemberRepository.save(projectMember));

            eventService.joinProject(project, user);

            logger.info("사용자 '{}' 를 프로젝트 '{}'에 '{}' 역할로 추가했습니다.", user.getName(), project.getName(), projectRole.getName());
        }

        //Interval?
        //boolean enabled = false;

        //FIXME 아래 코드와 RepositoryHelper 상의 파일 권한 동기화 개념이 충돌된다.
        //if (tcConfig.isSyncPermission()) {
            try {
                SvnSecurityHelper.updatePermission(tcConfig.getAuthz(), projectMembers.toArray(new ProjectMember[projectMembers.size()]));
            } catch (AuthzException e) {
                //TODO custom?
                throw new TeamcodeSecurityException(e);
            } catch (IOException e) {
                //TODO custom?
                throw new TeamcodeSecurityException(e);
            }
        //} else {
        //    logger.warn("사용자를 프로젝트에 추가했으나 사용자 정보 동기화 설정이 Off 상태이므로 서브버전 권한 동기화를 처리하지 못했습니다.");
       //}
    }

    @Transactional
    public void patchMember(final String projectPath, final Long memberId, ProjectMember projectMember) {
        logger.debug("프로젝트 '{}' 의 멤버 정보를 패치합니다...", projectPath);

        ProjectMember currentProjectMember = projectMemberRepository.findOne(memberId);
        if (projectMember.getRole() != null) {
            currentProjectMember.setRole(projectMember.getRole());
            logger.debug("멤버 역할을 '{}' 로 변경합니다.", projectMember.getRole());
        }

        currentProjectMember.setUpdatedAt(new Date());
        ProjectMember updatedProjectMember = projectMemberRepository.save(currentProjectMember);

        boolean enabled = false;
        if (enabled) {
            try {
                SvnSecurityHelper.updatePermission(tcConfig.getAuthz(), updatedProjectMember);
                logger.info("변경된 프로젝트 멤버 정보를 Authz 파일에 업데이트했습니다.");
            } catch (AuthzException e) {
                //TODO custom?
                throw new TeamcodeSecurityException(e);
            } catch (IOException e) {
                //TODO custom?
                throw new TeamcodeSecurityException(e);
            }
        }

        logger.info("프로젝트 '{}' 의 멤버 '{}' 정보를 패치했습니다.", projectPath, currentProjectMember.getUser().getName());
    }

    @Transactional
    public void removeMember(final String projectPath, final Long memberId) {
        Project project = getByPath(projectPath);
        ProjectMember projectMember = projectMemberRepository.findOne(memberId);
        if (projectMember != null) {
            projectMemberRepository.delete(projectMember);
            eventService.removeJoinEvent(project, projectMember.getUser());

            SvnSecurityHelper.deleteToRepository(tcConfig.getAuthz(), projectMember);
            logger.debug("Authz 파일에서 저장소에 설정된 사용자 권한 정보를 제거했습니다.");

            logger.info("프로젝트 '{}' 의 멤버 '{}' 를 삭제했습니다.", projectPath, projectMember.getUser().getFullName());
        }
        else {
            logger.warn("프로젝트 '{}' 의 멤버 삭제 요청을 받았으나 멤버 아이디 '{}' 에 해당하는 멤버를 찾을 수 없어 요청 처리를 건너뜁니다.", projectPath, memberId);
        }
    }

    /**
     *
     * @param projectPath
     * @param query 이 값은 <code>null</code> 일 수 있습니다. 사용자 이름, 아이디, 이메일에서 Contains 검색합니다.
     * @return
     */
    public List<User> getNotMembers(String projectPath, final String query) {
        Project project = getByPath(projectPath);
        logger.debug("프로젝트 '{}' 에 속하지 않은 사용자를 찾습니다. 검색어는 '{}' 입니다.", project.getName(), query);

        List<ProjectMember> projectMembers = projectMemberRepository.findByProject(project);
        List<User> users = userService.getActiveUsers(new Sort(new Sort.Order(Sort.Direction.ASC, "name")));//FIXME sort check

        if (users.size() > 0) {
            List<User> notMembers = users.stream().filter(u -> projectMembers.stream().noneMatch(p -> p.getUser().getId().longValue() == u.getId().longValue())).collect(Collectors.toList());
            if (StringUtils.hasText(query)) {
                //TODO 기본 이메일만 조회하고 있는데 모든 메일을 조회할 필요가 있을까?
                return notMembers.stream().filter(u -> {
                    return u.getName().toLowerCase().contains(query) || u.getFullName().toLowerCase().contains(query) || u.getEmail().contains(query);
                }).collect(Collectors.toList());
            }
            else {
                return notMembers;
            }
        }

        return users;
    }

    /**
     * 프로젝트를 아카이브합니다. 아래와 같은 순서로 처리가 됩니다.
     *
     * <ul>
     *  <li>프로젝트 path (데이터베이스 상)를 path-타임스탬프? 로 변경합니다.</li>
     *  <li>아카이브를 하면 서브버전 저장소 Root 를 repositories 에서 archived-repositories 로 변경합니다.</li>
     * </ul>
     * <p>나머지 첨부 파일들은 그대로 유지합니다. 어차피 Project ID 는 그대로 살려두어도 무방합니다.</p>
     * <p>프로젝트 삭제는 아키이브 후에 할 수 있습니다.</p>
     *
     * TODO 아카이브 한 뒤에 동일한 이름으로 프로젝트를 만들고 또 아카이브를 하면 어떻게 되는가?
     * TODO 아키이브 중에 누군가 커밋을 하면? 그냥 무시해도 되겠지?
     *
     * @param projectPath
     */
    @Transactional
    public void archive(final String projectPath) {
        User currentUser = userService.getCurrentUser();
        if (!currentUser.isAdmin())
            throw new TeamcodeSecurityException(String.format("권한이 없어 프로젝트 '%s' 를 아카이브할 수 없습니다.", projectPath));

        File prevArchivedRepository = new File(tcConfig.getArchivedRepositoryRootDir(), projectPath);
        if (prevArchivedRepository.exists())
            throw new TeamcodeException("프로젝트 '%s' 는 이미 아카이브되어 있습니다. 다시 아카이브하려면 삭제 후 하시기 바랍니다.");

        Project project = getByPath(projectPath);
        project.setPath(String.format("%s-%s", projectPath, DateUtils.archivedProjectNameSuffix()));
        project.setArchived(Boolean.TRUE);
        project.setArchivedAt(new Date());
        project.setUpdatedAt(new Date());

        projectRepository.save(project);

        try {
            SvnRepositoryHandler.archive(new File(tcConfig.getRepositoryRootDir(), projectPath), tcConfig.getArchivedRepositoryRootDir());
            boolean enabled = false;
            if (enabled) {
                SvnSecurityHelper.deleteRepository(tcConfig.getAuthz(), projectPath);
            }
        } catch (IOException e) {
            logger.error(String.format("오류가 발생하여 프로젝트 '%s' 를 아카이브할 수 없습니다.", projectPath));

            //TODO custom exception
            throw new TeamcodeException(e);
        }

        logger.info("프로젝트 '{}' 를 아키이브했습니다.", project.getName());
    }

    public List<ProjectLink> getProjectLinks(final String projectPath) {
        Project project = getByPath(projectPath);

        return projectLinkRepository.findByProject(project);
    }

    //TODO check privilege
    @Transactional
    public void addLink(final String projectPath, String title, String link) {
        Project project = getByPath(projectPath);
        logger.debug("프로젝트 '{}' 에 링크 '{}' 를 추가합니다...", project.getName(), link);

        if (!(link.startsWith("http://") || link.startsWith("https://"))) {
            link = String.format("http://%s", link);
        }

        ProjectLink projectLink = new ProjectLink();
        projectLink.setProject(project);
        projectLink.setTitle(title);
        projectLink.setLink(link);
        projectLink.setCreatedAt(new Date());
        projectLink.setUpdatedAt(new Date());

        projectLinkRepository.save(projectLink);
    }

    //TODO check privilege
    @Transactional
    public void deleteLink(Long projectLinkId) {
        ProjectLink projectLink = projectLinkRepository.findOne(projectLinkId);
        if (projectLink == null) {
            logger.warn("프로젝트 링크 (#{}) 이 존재하지 않아 삭제 요청을 건너뜁니다...", projectLinkId);

            return;
        }

        projectLinkRepository.delete(projectLinkId);
        logger.info("프로젝트 링크 (#{}) 를 삭제했습니다.", projectLinkId);
    }

}
