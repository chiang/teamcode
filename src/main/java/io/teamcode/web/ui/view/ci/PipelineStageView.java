package io.teamcode.web.ui.view.ci;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chiang on 2017. 4. 16..
 */
@Data
public class PipelineStageView {

    private String name;

    // /gitlab-org/gitlab-ce/pipelines/7659562#prepare
    private String path;

    private PipelineStatusView status;

    /**
     * 파이프라인 상세 페이지에서만 사용하는 속성. 파이프라인 목록에서는 사용하지 않습니다.
     *
     */
    @Setter(AccessLevel.NONE)
    private List<PipelineJobView> jobs = new ArrayList<>();

    //   prepare: running
    public String getTitle() {

        return new StringBuilder(this.name).append(": ").append(this.status.getLabel()).toString();
    }

    public void addPipelineJobView(PipelineJobView pipelineJobView) {
        this.jobs.add(pipelineJobView);
    }
}
