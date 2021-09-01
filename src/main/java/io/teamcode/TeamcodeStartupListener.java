package io.teamcode;

//import collabo.javahl.App;
import io.teamcode.common.TeamcodeConstants;
import io.teamcode.common.ci.RunnerHelper;
import io.teamcode.common.io.ClasspathResourceReader;
import io.teamcode.common.io.TeamcodePermissionHelper;
import io.teamcode.common.vcs.RepositoryHelper;
import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.*;
import io.teamcode.repository.ApplicationSettingRepository;
import io.teamcode.repository.UserRepository;
import io.teamcode.service.*;
import org.apache.commons.io.FilenameUtils;
import org.h2.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * Created by chiang on 2017. 3. 18..
 */
@Component
public class TeamcodeStartupListener {

    private static final Logger logger = LoggerFactory.getLogger(TeamcodeStartupListener.class);

    @Autowired
    TcConfig tcConfig;

    @Autowired
    ProjectService projectService;

    @Autowired
    UserService userService;

    @Autowired
    GroupService groupService;

    @Autowired
    TeamcodeExampleService teamcodeExampleService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ApplicationSettingRepository applicationSettingRepository;

    //TODO method name
    @PostConstruct
    public void evaluate() {
        logger.info("데이터베이스 기본 값들을 체크합니다...");

        verifyApplicationSetting();

        if (userRepository.findByName("lala") == null) {
            logger.info("테스트용 사용자 계정 (lala)를 새로 생성합니다...");
            User user = new User();
            user.setName("lala");
            user.setFullName("라라");
            user.setEmail("lala@example.com");
            user.setBio("시스템 설치 시 추가되는 기본 사용자입니다.");
            user.setUserRole(UserRole.ROLE_USER);
            user.setState(UserState.ACTIVE);
            user.setPassword("land");
            user.setCreatedAt(new Date());
            user.setUpdatedAt(new Date());

            userService.newUser(user);
        }

        User savedAdminUser = userRepository.findByName("admin");
        if (savedAdminUser == null) {
            logger.info("관리자 계정을 새로 생성합니다...");
            User adminUser = new User();
            adminUser.setName("admin");
            adminUser.setFullName("관리자");
            adminUser.setEmail("admin@example.com");
            adminUser.setUserRole(UserRole.ROLE_ADMIN);
            adminUser.setState(UserState.ACTIVE);
            adminUser.setPassword("h!5ive");
            adminUser.setBio("팀코드 시스템 전체를 관리하는 사용자입니다.");
            adminUser.setCreatedAt(new Date());
            adminUser.setUpdatedAt(new Date());

            savedAdminUser = userService.newUser(adminUser);
        }

        if (!StringUtils.hasText(savedAdminUser.getAvatarPath())) {
            File avatarsDir = new File(tcConfig.getAttachmentsDir(), "users");
            File currentAvatarDir = new File(avatarsDir, String.valueOf(savedAdminUser.getId()));

            if (!currentAvatarDir.exists()) {
                if (!currentAvatarDir.mkdirs())
                    throw new TeamcodeException(String.format("디렉터리를 만들 수 없습니다! %s", currentAvatarDir.getAbsolutePath()));
            }

            File avatarFile = new File(currentAvatarDir, "avatar.png");
            InputStream in = ClasspathResourceReader.getInputStream("public/assets/images/avatar/admin.png");
            try {

                IOUtils.copy(in, new FileOutputStream(avatarFile));

                savedAdminUser.setAvatarPath(String.format("users/%s/%s", savedAdminUser.getId(), avatarFile.getName()));
                userRepository.save(savedAdminUser);
            } catch (IOException e) {
                logger.error("사용자 아바타 파일을 저장하던 중 오류가 발생했습니다.", e);

                throw new TeamcodeException("사용자 아바타 파일을 저장하던 중 오류가 발생했습니다.", e);
            }
        }

        try {
            groupService.getByPath("projects");
        } catch (ResourceNotFoundException e) {
            Group group = new Group();
            group.setPath("projects");
            group.setType(GroupType.INTERNAL);
            group.setOwner(savedAdminUser);

            groupService.create(group);
        }


        try {
            teamcodeExampleService.install();
        } catch (IOException e) {
            throw new TeamcodeException("예제 프로젝트를 설치하던 중 오류가 발생했습니다.", e);
        }

        try {
            verifyRepository();
        } catch (IOException e) {
            //logger.error("서브버전 저장소를 검증하던 중 오류가 발행하였습니다.", e);
            throw new TeamcodeException("서브버전 저장소를 검증하던 중 오류가 발행하였습니다.", e);
        }

        logger.info("Teamcode 서버가 실행되었습니다: {}", tcConfig.getSubversionRealm());
    }

    private void verifyApplicationSetting() {
        if (applicationSettingRepository.count() == 0) {
            logger.info("TeamCode 설정 정보가 데이터베이스에 없습니다. 새로운 정보를 생성합니다...");

            ApplicationSetting applicationSetting = new ApplicationSetting();
            //applicationSetting.setRunnersRegistrationToken(RunnerHelper.generateRunnersRegistrationToken());
            //TODO 현재 버전에서는 여러 Runner 를 돌리지 않으므로 이렇게 설정합니다. (테스트 용도로 보면 됨).
            applicationSetting.setRunnersRegistrationToken("a4f9043d55921e9b40c8");
            applicationSetting.setCreatedAt(new Date());
            applicationSetting.setUpdatedAt(new Date());

            applicationSettingRepository.save(applicationSetting);
        }
    }

    /**
     * 전체 저장소에 설정된 Hook 들을 검증합니다. Hook Script 가 변경이 되었다면 당연히 덮어씁니다.
     *
     */
    private void verifyRepository() throws IOException {
        List<Project> projects = projectService.getNonArchived();
        logger.info("총 '{}' 개 프로젝트의 서브버전 저장소 검증을 시작합니다...", projects.size());

        String repositoryPostCommitHookUrl;

        File repositoryDir;
        for (Project project: projects) {
            repositoryDir = new File(tcConfig.getRepositoryRootDir(), project.getPath());
            if (!repositoryDir.exists()) {
                logger.warn("프로젝트 '{}' 는 서브버전 저장소 디렉터리가 없습니다. 검증을 건너뜁니다...", project.getPath());
                continue;
            }

            if (tcConfig.isSyncPermission()) {
                logger.debug("서브버전 저장소의 소유자 권한을 체크합니다...");
                TeamcodePermissionHelper.validateRepositoryDirectoryThenSetOwner(repositoryDir);
            }
            else {
                logger.debug("서브버전 저장소 소유자 권한 체크 기능이 Off 상태입니다. 권한 체크를 건너뜁니다.");
            }

            repositoryPostCommitHookUrl = RepositoryHelper.buildHookEndpoint(tcConfig.getExternalUrl(), project.getPath(), "");

            if (logger.isDebugEnabled()) {
                logger.debug("'{}' 저장소의 Hook Script 를 검증합니다.", project.getPath());
                logger.debug("'{}' 저장소의 Post Commit Hook URL: <{}>", project.getPath(), repositoryPostCommitHookUrl);
            }

            RepositoryHelper.recoveryIfInvalidPostCommitHook(project.getPath(), tcConfig.getRepositoryRootDir(), repositoryPostCommitHookUrl, tcConfig.isSyncPermission());
            logger.debug("'{}' 저장소의 pre-revprop-change 스크립트를 동기화합니다...", project.getPath());
            RepositoryHelper.syncPreRevpropChangeScript(repositoryDir, tcConfig.isSyncPermission());
        }

    }
}
