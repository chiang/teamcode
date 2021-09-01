package io.teamcode.service.project.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.teamcode.domain.entity.project.integration.ProjectIntegrationServiceSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.util.StringUtils;

import java.util.*;

public abstract class AbstractProjectIntegrationService implements ProjectIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectIntegrationService.class);

    protected static final List<String> PROPS = new ArrayList<>();

    @Override
    public List<String> getPropertyNames() {

        return PROPS;
    }

    @Override
    public ProjectIntegrationServiceSettings getDefaultSettings() {
        ProjectIntegrationServiceSettings settings = new ProjectIntegrationServiceSettings();
        settings.setKey(getKey());
        settings.setActive(Boolean.FALSE);
        settings.setCategory(this.getCategory());
        settings.setTitle(getTitle());
        settings.setDescription(getDescription());
        settings.setLogoPath(getLogoPath());

        return settings;
    }

    @Override
    public void patch(ProjectIntegrationServiceSettings projectIntegrationServiceSettings, Map<String, String[]> parameters) {
        projectIntegrationServiceSettings.setUpdatedAt(new Date());
        if (parameters.containsKey("active")) {
            projectIntegrationServiceSettings.setActive(Boolean.valueOf(parameters.get("active")[0]));
        }
        else {
            projectIntegrationServiceSettings.setActive(Boolean.FALSE);
        }
        buildProperties(projectIntegrationServiceSettings, parameters);
        doPatch(projectIntegrationServiceSettings, parameters);

        logger.debug("프로젝트 '{}' 의 서비스 '{}' 설정을 Patch 했습니다. Json Property: {}",
                projectIntegrationServiceSettings.getProject().getName(),
                projectIntegrationServiceSettings.getKey(),
                projectIntegrationServiceSettings.getProperties());
    }

    protected abstract void doBuildProperties(Map<String, Object> serviceProperties, Map<String, String[]> parameters);

    protected abstract void doPatch(ProjectIntegrationServiceSettings projectIntegrationServiceSettings, Map<String, String[]> parameters);


    private void buildProperties(ProjectIntegrationServiceSettings projectIntegrationServiceSettings, Map<String, String[]> parameters) {
        Map<String, Object> serviceProperties = buildServiceProperties(projectIntegrationServiceSettings);

        doBuildProperties(serviceProperties, parameters);
        setServiceProperties(projectIntegrationServiceSettings, serviceProperties);
    }

    private Map<String, Object> buildServiceProperties(ProjectIntegrationServiceSettings projectIntegrationServiceSettings) {
        Map<String, Object> serviceProperties;
        if (StringUtils.hasText(projectIntegrationServiceSettings.getProperties())) {
            JacksonJsonParser jsonParser = new JacksonJsonParser();
            serviceProperties = jsonParser.parseMap(projectIntegrationServiceSettings.getProperties());
        }
        else {
            serviceProperties = new HashMap<>();
        }

        return serviceProperties;
    }

    private void setServiceProperties(ProjectIntegrationServiceSettings projectIntegrationServiceSettings, Map<String, Object> serviceProperties) {
        try {
            String json = new ObjectMapper().writeValueAsString(serviceProperties);
            projectIntegrationServiceSettings.setProperties(json);
        } catch (JsonProcessingException e) {
            throw new ProjectIntegrationServiceException(e);
        }
    }
}
