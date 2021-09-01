package io.teamcode.web.api.model.ci;

import io.teamcode.domain.entity.ci.PipelineStatus;
import lombok.Data;

/**
 * Created by chiang on 2017. 5. 14..
 */
@Data
public class UpdateJobRequest {

    private String token;

    /**
     * Runner 에서 보낸 JobState 값. 이 값이 PipelineStatus 와 매칭이 됨.
     */
    private PipelineStatus state;

    /**
     * Build Full Trace. 이 값이 오면 Append 하지 않고 통으로 덮어씁니다.
     */
    private String trace;

}
