package io.teamcode.common.ci;

import lombok.Data;

/**
 * Runner 에서 보내는 Request Root?
 *
 */
@Data
public class RunnerRequest {

    private String token;

    private VersionInfo info;

}