package io.teamcode.web.ui.view.ci;

import io.teamcode.common.vcs.svn.Commit;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by chiang on 2017. 4. 12..
 */
@Data
public class PipelineView {

    private Long id;

    /**
     * UI 에서 직접 접근할 수 있는 경로 정보
     */
    private String path;

    /**
     * UI 에서 직접 취소시킬 수 있는 경로. Gitlab 에서는 /baramboy/jandi-connector/pipelines/7774545/cancel 와 같이 호출함.
     *
     */
    private String cancelPath;

    /**
     * UI 에서 직접 재시도할 수 있는 경로. Gitlab 에서는 /baramboy/jandi-connector/pipelines/7774545/retry 와 같이 호출함.
     *
     */
    private String retryPath;

    private Commit commit;

    private PipelineDetailsView details;

    private PipelineFlags flags;

    private String yamlErrors;

    private Date createdAt;

    private Date updatedAt;

    public int countJobs() {

        if (this.details != null && this.details.getStages() != null) {
            return this.details.getStages().stream().mapToInt(s -> s.getJobs().size()).sum();
        }

        return 0;
    }

}
