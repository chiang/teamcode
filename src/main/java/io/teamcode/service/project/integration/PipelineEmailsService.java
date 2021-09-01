package io.teamcode.service.project.integration;

import io.teamcode.domain.entity.project.integration.ProjectIntegrationServiceCategory;
import io.teamcode.domain.entity.project.integration.ProjectIntegrationServiceSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service(PipelineEmailsService.KEY)
@Transactional(readOnly = true)
public class PipelineEmailsService extends AbstractProjectIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(PipelineEmailsService.class);

    public static final String KEY = "pipeline_emails";

    @Override
    protected void doBuildProperties(Map<String, Object> serviceProperties, Map<String, String[]> parameters) {

    }

    @Override
    protected void doPatch(ProjectIntegrationServiceSettings projectIntegrationServiceSettings, Map<String, String[]> parameters) {

    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getTitle() {
        return "파이프라인 이메일";
    }

    @Override
    public String getDescription() {
        return "빌드 및 배포 파이프라인 상태 정보를 지정된 수신자에게 이메일로 발송합니다.";
    }

    @Override
    public String getLogoPath() {
        return "/assets/images/logos/logo-integration-default.svg";
    }

    @Override
    public ProjectIntegrationServiceCategory getCategory() {
        return ProjectIntegrationServiceCategory.EMAIL;
    }

    @Override
    public void execute(String projectPath, ProjectIntegrationServiceSettings projectIntegrationServiceSettings) {

    }
}
