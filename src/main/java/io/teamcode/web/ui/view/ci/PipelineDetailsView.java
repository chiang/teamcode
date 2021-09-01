package io.teamcode.web.ui.view.ci;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by chiang on 2017. 4. 12..
 */
@Data
public class PipelineDetailsView {

    private Long duration;

    private Date finishedAt;

    //TODO
    private List<String> manualActions = Collections.emptyList();

    //TODO to Artifact Object
    private List<String> artifacts = Collections.emptyList();

    @Setter(AccessLevel.NONE)
    private List<PipelineStageView> stages = new ArrayList<>();

    private PipelineStatusView status;

    public void addStage(PipelineStageView pipelineStageView) {
        this.stages.add(pipelineStageView);
    }
}
