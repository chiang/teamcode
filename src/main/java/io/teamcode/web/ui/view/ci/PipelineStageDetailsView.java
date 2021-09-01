package io.teamcode.web.ui.view.ci;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 파이프라인 스테이지 세부 내용
 */
@Data
public class PipelineStageDetailsView {

    @Setter(AccessLevel.NONE)
    private List<PipelineJobView> jobs = new ArrayList<>();

    public void addPipelineJobView(PipelineJobView pipelineJobView) {
        this.jobs.add(pipelineJobView);
    }

}
