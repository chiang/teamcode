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
@Service(BugzillaService.KEY)
@Transactional(readOnly = true)
public class BugzillaService extends AbstractProjectIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(BugzillaService.class);

    public static final String KEY = "bugzilla";

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
        return "Bugzilla";
    }

    @Override
    public String getDescription() {
        return "Bugzilla Issue Tracker";
    }

    @Override
    public String getLogoPath() {
        return "/assets/images/logos/logo-bugzilla@2x.png";
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
        logger.debug("???????????? ????????? '{}' ??? ???????????????...", this.getTitle());

        Project project = projectService.getByPath(projectPath);

        if (!StringUtils.hasText(projectIntegrationServiceSettings.getProperties())) {
            logger.warn("????????? '{}' ??? ?????? ????????? ?????? ???????????? ????????? ??? ????????????.", this.getTitle());
            return;
        }

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        Map<String, Object> serviceProperties = jsonParser.parseMap(projectIntegrationServiceSettings.getProperties());
        if (!serviceProperties.containsKey(RECIPIENTS_PROPERTY_NAME)) {
            logger.warn("????????? '{}' ??? '{}' ????????? ?????? ???????????? ????????? ??? ????????????.", this.getTitle(), RECIPIENTS_PROPERTY_NAME);
            return;
        }

        String[] recipients = (String[])serviceProperties.get(RECIPIENTS_PROPERTY_NAME);
    }

}
