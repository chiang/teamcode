package io.teamcode.web.ui.view.ci;

import io.teamcode.domain.entity.ci.JobAction;
import io.teamcode.domain.entity.ci.PipelineStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chiang on 2017. 5. 14..
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobStatusView {

    private PipelineStatus jobStatus;

    public String getGroup() {
        return this.jobStatus.getGroup();
    }

    public String getIcon() {
        return this.jobStatus.getIcon();
    }

    public String getText() {
        return this.jobStatus.getText();
    }

    public boolean isHasAction() {
        return this.jobStatus.getJobAction() != null;
    }

    public JobActionView getJobAction() {

        if (this.isHasAction())
            return JobActionView.builder().icon(this.jobStatus.getJobAction().getIcon()).build();
        else
            return null;
    }
}
