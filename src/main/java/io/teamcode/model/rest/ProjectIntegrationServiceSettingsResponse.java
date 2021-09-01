package io.teamcode.model.rest;

import io.teamcode.domain.entity.project.integration.ProjectIntegrationServiceSettings;
import lombok.Data;
import org.springframework.hateoas.ResourceSupport;

@Data
public class ProjectIntegrationServiceSettingsResponse extends ResourceSupport {

    private ProjectIntegrationServiceSettings settings;

}
