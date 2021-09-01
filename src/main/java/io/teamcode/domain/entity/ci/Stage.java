package io.teamcode.domain.entity.ci;

import lombok.Data;

import java.util.List;

/**
 * Created by chiang on 2017. 4. 16..
 */
@Data
public class Stage {

    private String name;

    private PipelineStatus status;

    private List<Job> jobs;

    /**
     * 현재 작업 중인 Stage 인지 여부를 반환합니다.
     *
     */
    private boolean current;

}
