package io.teamcode.web.ui.view.ci;

import io.teamcode.common.vcs.svn.Commit;
import io.teamcode.domain.entity.ci.Job;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Job 상세 페이지에서 사용하는 View.
 */
@Data
public class JobView {

    private Long pipelineId;

    private Job job;

    private String pipelineLink;

    private String jobTraceLink;

    private String commitLink;

    private Commit commit;

    @Setter(AccessLevel.NONE)
    private List<PipelineStageView> stages = new ArrayList<>();

    public void addStage(PipelineStageView pipelineStageView) {
        this.stages.add(pipelineStageView);
    }
}
