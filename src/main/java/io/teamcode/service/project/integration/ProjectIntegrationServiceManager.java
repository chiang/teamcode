package io.teamcode.service.project.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.teamcode.common.vcs.svn.callback.SummaryLogMessage;
import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.project.integration.ProjectIntegrationServiceSettings;
import io.teamcode.repository.ProjectIntegrationServiceRepository;
import io.teamcode.service.ProjectService;
import io.teamcode.service.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.context.ApplicationContext;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by chiang on 2017. 4. 9..
 */
@Service
@Transactional(readOnly = true)
public class ProjectIntegrationServiceManager {

    private static final Logger logger = LoggerFactory.getLogger(ProjectIntegrationServiceManager.class);

    private static final Map<String, Class> availableServices = new LinkedHashMap<>();

    static {
        availableServices.put(EmailsOnCommitService.KEY, EmailsOnCommitService.class);
        availableServices.put(PipelineEmailsService.KEY, PipelineEmailsService.class);
        availableServices.put(CustomIssueTracker.KEY, CustomIssueTracker.class);
        availableServices.put(RedmineService.KEY, RedmineService.class);
        availableServices.put(BugzillaService.KEY, BugzillaService.class);
        availableServices.put(SlackService.KEY, SlackService.class);
    }

    @Autowired
    ApplicationContext context;

    @Autowired
    ProjectService projectService;

    @Autowired
    ProjectIntegrationServiceRepository projectIntegrationServiceRepository;

    public List<ProjectIntegrationServiceSettings> getAllSettings(String projectPath) {
        List<ProjectIntegrationServiceSettings> allSettings = new ArrayList<>();

        Project project = projectService.getByPath(projectPath);
        for (String key: availableServices.keySet()) {
            allSettings.add(getSettingsIfNotExistThenDefault(project, key));
        }

        return allSettings;
    }

    public ProjectIntegrationServiceSettings getSettings(String projectPath, String serviceKey) {
        Project project = projectService.getByPath(projectPath);
        ProjectIntegrationServiceSettings settings = projectIntegrationServiceRepository.findByProjectAndKey(project, serviceKey);
        if (settings == null) {
            throw new ResourceNotFoundException(String.format("프로젝트 '%s' 에서 서비스 키 '%s' 에 대한 서비스 설정 정보를 찾을 수 없습니다.", projectPath, serviceKey));
        }

        ProjectIntegrationService projectIntegrationService = (ProjectIntegrationService)context.getBean(serviceKey);
        settings.setLogoPath(projectIntegrationService.getLogoPath());

        return settings;
    }

    public ProjectIntegrationServiceSettings getSettingsIfNotExistThenDefault(String projectPath, String serviceKey) {
        Project project = projectService.getByPath(projectPath);

        return getSettingsIfNotExistThenDefault(project, serviceKey);
    }


    public ProjectIntegrationServiceSettings getSettingsIfNotExistThenDefault(Project project, String serviceKey) {
        try {

            return getSettings(project.getPath(), serviceKey);
        } catch (ResourceNotFoundException e) {
            ProjectIntegrationService projectIntegrationService = (ProjectIntegrationService)context.getBean(serviceKey);

            ProjectIntegrationServiceSettings defaultSettings = projectIntegrationService.getDefaultSettings();
            defaultSettings.setKey(serviceKey);
            defaultSettings.setCreatedAt(new Date());
            defaultSettings.setProject(project);

            return defaultSettings;
        }
    }

    @Transactional
    public void patchSettings(final String projectPath, String serviceKey, Map<String, String[]> parameters) {
        Project project = projectService.getByPath(projectPath);
        ProjectIntegrationService projectIntegrationService = (ProjectIntegrationService)context.getBean(serviceKey);

        ProjectIntegrationServiceSettings settings = getSettingsIfNotExistThenDefault(project, serviceKey);
        projectIntegrationService.patch(settings, parameters);
    }

    public void executeServices(final String projectPath, final SummaryLogMessage summaryLogMessage, final long revisionNumber) {
        Project project = projectService.getByPath(projectPath);

        List<ProjectIntegrationServiceSettings> projectIntegrationServiceSettings
                = projectIntegrationServiceRepository.findByProjectAndActiveAndCommitEvents(project, Boolean.TRUE, Boolean.TRUE);

        if (projectIntegrationServiceSettings.size() == 0) {
            logger.debug("프로젝트 '{}' 에 등록된 서비스가 없어 요청을 건너뜁니다.", projectPath);

            return;
        }

        ProjectIntegrationService projectIntegrationService;
        for (ProjectIntegrationServiceSettings ps : projectIntegrationServiceSettings) {
            logger.debug("프로젝트 '{}' 에 등록된 서비스 '{}' 를 실행합니다...", projectPath, ps.getTitle());

            ps.setSummaryLogMessage(summaryLogMessage);//FIXME 매번 넣어주나?
            ps.setRevision(revisionNumber);

            projectIntegrationService = (ProjectIntegrationService)context.getBean(ps.getKey());

            if (projectIntegrationService != null) {
                projectIntegrationService.execute(projectPath, ps);
            }
        }
    }
}
