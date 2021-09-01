package io.teamcode.service;

import io.teamcode.common.vcs.RepositoryHelper;
import io.teamcode.common.vcs.svn.SvnRepositoryHandler;
import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.ProgrammingLanguage;
import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.Visibility;
import io.teamcode.repository.ProjectRepository;
import io.teamcode.service.security.ImpersonateService;
import io.teamcode.util.FileSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by chiang on 2017. 3. 18..
 */
@Service
public class TeamcodeExampleService {

    private static final Logger logger = LoggerFactory.getLogger(TeamcodeExampleService.class);

    //TODO 설정으로?
    private final String exampleProjectName = "example";

    @Autowired
    TcConfig tcConfig;

    @Autowired
    ProjectService projectService;

    @Autowired
    ImpersonateService impersonateService;

    @Autowired
    AvatarService avatarService;

    //@Autowired
//    SvnRepositoryImportService svnRepositoryImportService;

    @Autowired
    ProjectRepository projectRepository;

    @Transactional
    public void install() throws IOException {
        File repositoryDirectory = new File(tcConfig.getRepositoryRootDir(), exampleProjectName);

        impersonateService.switchAdmin();

        try {
            if (repositoryDirectory.exists()) {
                Project project = projectRepository.findByPath(exampleProjectName);
                if (project != null) {
                    logger.debug("예제 프로젝트가 설치되어 있습니다. 예제 프로젝트 설치를 건너뜁니다...");
                } else {
                    logger.info("예제 Repository 가 '{}' 에 설치되어 있으나 프로젝트로 등록이 되지 않았습니다. 프로젝트 등록을 시작합니다...", repositoryDirectory.getAbsolutePath());

                    project = new Project();
                    project.setName(exampleProjectName);
                    project.setPath(exampleProjectName);
                    project.setDescription("TeamCode 기능을 살펴볼 수 있는 예제 프로젝트입니다. Twitter Bootstrap Clone 입니다 :)");
                    project.setVisibility(Visibility.PUBLIC);
                    project.setProgrammingLanguage(ProgrammingLanguage.JAVA);

                    project = projectService.create(null, project);
                    //avatarService.storeAvatar(project.getId());

                    //svnRepositoryImportService.importRepository(exampleProjectName);

                }
                String repositoryPostCommitHookUrl = RepositoryHelper.buildHookEndpoint(tcConfig.getExternalUrl(), repositoryDirectory.getName(), "");
                RepositoryHelper.recoveryIfInvalidPostCommitHook(repositoryDirectory.getName(), repositoryDirectory.getParentFile(), repositoryPostCommitHookUrl, tcConfig.isSyncPermission());
            } else {
                logger.info("예제 프로젝트 설치를 시작합니다...");

                InputStream inputStream;
                if (tcConfig.getSubversionVersion().equals("1.8")) {
                    logger.debug("예제 Repository 를 Subversion 1.8 용으로 설정합니다...");
                    inputStream = SvnRepositoryHandler.class.getResourceAsStream("/example-repository-1.8.zip");
                }
                else
                    inputStream = SvnRepositoryHandler.class.getResourceAsStream("/example-repository-1.9.zip");

                FileSystemUtils.unzip(inputStream, repositoryDirectory);
                logger.debug("예제 프로젝트 저장소 이름: {}", repositoryDirectory.getName());

                String repositoryPostCommitHookUrl = RepositoryHelper.buildHookEndpoint(tcConfig.getExternalUrl(), repositoryDirectory.getName(), "");

                logger.debug("'{}' Repository 의 Hook Script 를 검증합니다.", repositoryDirectory.getName());
                logger.debug("'{}' Repository 의 Post Commit Hook URL: <{}>", repositoryDirectory.getName(), repositoryPostCommitHookUrl);

                RepositoryHelper.recoveryIfInvalidPostCommitHook(repositoryDirectory.getName(), repositoryDirectory.getParentFile(), repositoryPostCommitHookUrl, tcConfig.isSyncPermission());

                Project project = new Project();
                project.setPath(exampleProjectName);
                project.setDescription("예제 프로젝트입니다.");
                project = projectService.create(null, project);
                avatarService.storeAvatar(project.getId());
            }
        } finally {
            impersonateService.clearAuthentiation();
        }
    }
}
