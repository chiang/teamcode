package io.teamcode.web.ui.view.ci;

import io.teamcode.domain.entity.ci.PipelineStatus;
import lombok.*;

/**
 * Created by chiang on 2017. 4. 16..
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStatusView {

    private PipelineStatus pipelineStatus;

    //"/gitlab-org/gitlab-ce/pipelines/7659562#prepare"
    private String detailsPath;

    @Getter(AccessLevel.NONE)
    private boolean hasDetails;

    /**
     * Gitlab Style:
     *
     * subject.target_url.present? &&can?(user, :read_commit_status, subject)
     *
     *
     * @return
     */
    public boolean hasDetails() {

        return this.hasDetails;
    }

    public String getLabel() {

        return this.pipelineStatus.getLabel();
    }

    public String getText() {

        return this.pipelineStatus.getText();
    }

    public String getIcon() {
        return this.pipelineStatus.getIcon();
    }

    public String getGroup() {
        return this.pipelineStatus.getGroup();
    }

}
