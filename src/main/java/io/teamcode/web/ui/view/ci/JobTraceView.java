package io.teamcode.web.ui.view.ci;

import io.teamcode.domain.entity.ci.PipelineStatus;
import lombok.Data;

/**
 * Created by chiang on 2017. 5. 17..
 *
 *
 *
 */
@Data
public class JobTraceView {

    /**
     * Job 아이디
     */
    private Long id;

    private boolean complete;

    private String html;

    private int offset;

    private long size;

    private boolean truncated;

    /**
     * 현재 Job 의 상태.
     */
    private PipelineStatus status;
}
