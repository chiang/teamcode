package io.teamcode.web.api.model.ci;

import io.teamcode.domain.entity.ci.JobWhen;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Step {

    private String name;

    private List<String> scripts;

    /**
     * 빌드 타임아웃. 초 단위.
     *
     */
    private int timeout;

    private JobWhen when;

    private boolean allowFailure = false;

    /*private

    Name         StepName   `json:"name"`
    Script       StepScript `json:"script"`
    Timeout      int        `json:"timeout"`
    When         StepWhen   `json:"when"`
    AllowFailure bool       `json:"allow_failure"`*/
}
