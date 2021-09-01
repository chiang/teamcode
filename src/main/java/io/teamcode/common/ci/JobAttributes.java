package io.teamcode.common.ci;

import io.teamcode.common.ci.config.entry.StageEntry;
import io.teamcode.domain.entity.ci.JobWhen;
import lombok.Data;

/**
 * Created by chiang on 2017. 4. 12..
 */
@Data
public class JobAttributes {

    private String name;

    private int stageIndex;

    private StageEntry stage;

    private JobWhen jobWhen;

    //private List<Commands>

    /*
    job = @jobs[name.to_sym] || {}
      {
        stage_idx: @stages.index(job[:stage]),
        stage: job[:stage],
        commands: job[:commands],
        tag_list: job[:tags] || [],
        name: job[:name].to_s,
        allow_failure: job[:ignore],
        environment: job[:environment_name],
        coverage_regex: job[:coverage],
        yaml_variables: yaml_variables(name),
        options: {
          image: job[:image],
          services: job[:services],
          artifactsFile: job[:artifactsFile],
          cache: job[:cache],
          dependencies: job[:dependencies],
          after_script: job[:after_script],
          environment: job[:environment],
        }.compact
      }
     */
}
