package io.teamcode.web.api.model.ci;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.teamcode.domain.entity.ci.Job;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chiang on 2017. 5. 11..
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {

    @Getter(onMethod = @__( @JsonIgnore))
    private Job job;

    /**
     *
     * Job ID
     */
    private Long id;

    /**
     * 이 Job 이 속한 Pipeline 아이디입니다.
     *
     */
    private Long pipelineId;

    private String token;

    private JobInfo jobInfo;

    private RepositoryInfo repositoryInfo;

    private String image;

    private List<Step> steps = new ArrayList<>();

    private List<Variable> variables = new ArrayList<>();

    private Artifact artifact;

    private List<Dependency> dependencies;

    public void addStep(final Step step) {
        this.steps.add(step);
    }

    public void addJobVariable(final Variable variable) {
        this.variables.add(variable);
    }

}
