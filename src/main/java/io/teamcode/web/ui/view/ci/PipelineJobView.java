package io.teamcode.web.ui.view.ci;

import io.teamcode.domain.entity.ci.PipelineStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by chiang on 2017. 4. 25..
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineJobView {

    private Long id;

    private String name;

    private JobStatusView status;

    private String path;

    /**
     * retry, play 등을 실행할 수 있는 경로 정보
     *
     */
    private String actionPath;

    private String duration;

    private Date finishedAt;
}
