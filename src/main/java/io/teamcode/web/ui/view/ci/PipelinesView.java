package io.teamcode.web.ui.view.ci;

import io.teamcode.web.ui.view.ci.PipelinesCountView;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Root View.
 *
 * PipelinesView > PipelineView > PipelineDetailsView 순으로 포함합니다.
 *
 *
 */
@Data
public class PipelinesView {

    private PipelinesCountView count;

    @Setter(AccessLevel.NONE)
    private List<PipelineView> pipelines = new ArrayList<>();

    //private Page<PipelineView> pipelinesPage;

    public void addPipelineView(PipelineView pipelineView) {
        this.pipelines.add(pipelineView);
    }

}
