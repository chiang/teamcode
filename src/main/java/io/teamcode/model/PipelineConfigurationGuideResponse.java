package io.teamcode.model;

import lombok.Data;
import org.springframework.hateoas.ResourceSupport;

@Data
public class PipelineConfigurationGuideResponse extends ResourceSupport {

    private boolean hasPipelineYml;

}
