package io.teamcode.service.project.integration;

import io.teamcode.common.vcs.svn.SvnCommandHelper;
import io.teamcode.common.vcs.svn.SvnUtils;
import io.teamcode.config.TcConfig;
import io.teamcode.domain.entity.Project;
import io.teamcode.domain.entity.project.integration.ProjectIntegrationServiceCategory;
import io.teamcode.domain.entity.project.integration.ProjectIntegrationServiceSettings;
import io.teamcode.repository.ProjectIntegrationServiceRepository;
import io.teamcode.service.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service(CustomIssueTracker.KEY)
@Transactional(readOnly = true)
public class CustomIssueTracker extends AbstractProjectIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailsOnCommitService.class);

    public static final String KEY = "custom_issue_tracker";

    public static final String REV_PRO_NAME = "tc:issuetracker:id";

    public static final String PROPS_REGEXP = "regexp";

    public static final String PROPS_URL = "url";

    public static final String PROPS_LINK_ENABLED = "linkEnabled";

    static {
        PROPS.add(PROPS_REGEXP);
        PROPS.add(PROPS_URL);
        PROPS.add(PROPS_LINK_ENABLED);
    }

    @Autowired
    TcConfig tcConfig;

    @Autowired
    ProjectService projectService;

    @Autowired
    ProjectIntegrationServiceRepository projectIntegrationServiceRepository;

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public String getTitle() {
        return "Custom Issue Tracker";
    }

    @Override
    public String getDescription() {
        return "이슈 트래커 설정입니다.";
    }

    @Override
    public String getLogoPath() {
        return "/assets/images/logos/logo-integration-default.svg";
    }

    @Override
    public List<String> getPropertyNames() {

        return PROPS;
    }

    @Override
    public ProjectIntegrationServiceCategory getCategory() {

        return ProjectIntegrationServiceCategory.ISSUE_TRACKER;
    }

    @Override
    @Transactional
    protected void doPatch(ProjectIntegrationServiceSettings projectIntegrationServiceSettings, Map<String, String[]> parameters) {
        if (parameters.containsKey("title")) {
            projectIntegrationServiceSettings.setTitle(parameters.get("title")[0]);
        }

        if (parameters.containsKey("description")) {
            projectIntegrationServiceSettings.setDescription(parameters.get("description")[0]);
        }
        //설정하면 무조건 커밋 시 동작해야 합니다.
        projectIntegrationServiceSettings.setCommitEvents(Boolean.TRUE);

        projectIntegrationServiceRepository.save(projectIntegrationServiceSettings);
    }

    @Override
    public void doBuildProperties(Map<String, Object> serviceProperties, Map<String, String[]> parameters) {
        for (String propKey: this.getPropertyNames()) {
            if (parameters.containsKey(propKey)) {
                serviceProperties.put(propKey, parameters.get(propKey)[0]);
            } else {
                serviceProperties.remove(propKey);
            }
        }
    }

    @Override
    public void execute(String projectPath, ProjectIntegrationServiceSettings projectIntegrationServiceSettings) {
        logger.debug("프로젝트 서비스 '{}' 를 실행합니다...", this.getTitle());

        Project project = projectService.getByPath(projectPath);

        if (!StringUtils.hasText(projectIntegrationServiceSettings.getProperties())) {
            logger.warn("서비스 '{}' 에 설정 정보가 없어 서비스를 실행할 수 없습니다.", this.getTitle());
            return;
        }

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        Map<String, Object> serviceProperties = jsonParser.parseMap(projectIntegrationServiceSettings.getProperties());

        String regExp = null;
        if (serviceProperties.containsKey(PROPS_REGEXP)) {
            regExp = (String)serviceProperties.get(PROPS_REGEXP);
        }

        if (StringUtils.hasText(regExp)) {
            String extractedIssueId = extractIssueId(regExp, projectIntegrationServiceSettings.getSummaryLogMessage().getMessage());
            logger.debug("[{}] 추출된 이슈 아이디: {}", getKey(), extractedIssueId);

            if (StringUtils.hasText(extractedIssueId)) {
                String repositoryUri = SvnUtils.buildAbsoluteUrlAsFileScheme(tcConfig.getRepositoryRootPath(), projectPath, "");
                SvnCommandHelper.addRevisionProperty(repositoryUri, projectIntegrationServiceSettings.getRevision(), REV_PRO_NAME, extractedIssueId);
                logger.debug("Revision property 를 설정했습니다.");
            }
        }
    }

    private String extractIssueId(String regexp, String logMessage) {
        if (StringUtils.hasText(logMessage)) {
            Pattern p = Pattern.compile(regexp);
            Matcher m = p.matcher(logMessage);
            if (m.find()) {
                return m.group(1);
            }
        }

        return null;
    }
}
