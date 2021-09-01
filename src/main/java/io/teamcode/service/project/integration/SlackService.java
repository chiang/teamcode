package io.teamcode.service.project.integration;

import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.project.integration.ProjectIntegrationServiceCategory;
import io.teamcode.domain.entity.project.integration.ProjectIntegrationServiceSettings;
import io.teamcode.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chiang on 2017. 4. 8..
 */
@Service(SlackService.KEY)
@Transactional(readOnly = true)
public class SlackService extends AbstractProjectIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(SlackService.class);

    public static final String KEY = "slack";

    private static final String RECIPIENTS_PROPERTY_NAME = "recipients";

    static {
        PROPS.add(RECIPIENTS_PROPERTY_NAME);
    }

    @Autowired
    JavaMailSender mailSender;

    @Autowired
    ProjectService projectService;

    @Override
    public String getKey() {

        return KEY;
    }

    @Override
    public String getTitle() {
        return "Slack Notifications";
    }

    @Override
    public String getDescription() {
        return "Receive event notifications in Slack";
    }

    @Override
    public String getLogoPath() {
        return "/assets/images/logos/logo-slack.svg";
    }

    @Override
    public List<String> getPropertyNames() {

        return PROPS;
    }

    @Override
    public ProjectIntegrationServiceCategory getCategory() {

        return ProjectIntegrationServiceCategory.EMAIL;
    }

    @Override
    protected void doBuildProperties(Map<String, Object> serviceProperties, Map<String, String[]> parameters) {

    }

    @Override
    protected void doPatch(ProjectIntegrationServiceSettings projectIntegrationServiceSettings, Map<String, String[]> parameters) {

    }

    public void execute(final String projectPath, final ProjectIntegrationServiceSettings projectIntegrationServiceSettings) {
        logger.debug("프로젝트 서비스 '{}' 를 실행합니다...", this.getTitle());

        Project project = projectService.getByPath(projectPath);

        if (!StringUtils.hasText(projectIntegrationServiceSettings.getProperties())) {
            logger.warn("서비스 '{}' 에 설정 정보가 없어 서비스를 실행할 수 없습니다.", this.getTitle());
            return;
        }

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        Map<String, Object> serviceProperties = jsonParser.parseMap(projectIntegrationServiceSettings.getProperties());
        if (!serviceProperties.containsKey(RECIPIENTS_PROPERTY_NAME)) {
            logger.warn("서비스 '{}' 에 '{}' 정보가 없어 서비스를 실행할 수 없습니다.", this.getTitle(), RECIPIENTS_PROPERTY_NAME);
            return;
        }

        String[] recipients = (String[])serviceProperties.get(RECIPIENTS_PROPERTY_NAME);
    }



}
