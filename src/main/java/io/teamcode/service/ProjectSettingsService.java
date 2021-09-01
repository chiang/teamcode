package io.teamcode.service;

import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.ProjectMenuVisibility;
import io.teamcode.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 프로젝트 별 설정을 처리하는 서비스
 */
@Service
@Transactional(readOnly = true)
public class ProjectSettingsService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectSettingsService.class);

    @Autowired
    ProjectService projectService;

    @Autowired
    ProjectRepository projectRepository;

    //TODO check members
    @Transactional
    public void togglePipeline(final String projectPath, boolean enabled) {
        Project exist = projectService.getByPath(projectPath);
        if (enabled)
            exist.setPipelineVisibility(ProjectMenuVisibility.ENABLED);
        else
            exist.setPipelineVisibility(ProjectMenuVisibility.DISABLED);

        projectRepository.save(exist);
        logger.info("프로젝트 '{}' 의 파이프라인 설정을 '{}' 로 설정했습니다.", projectPath, enabled);
    }
}
