package io.teamcode.web.api.model.ci;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 이전 Stage 에서 만들어진 Artifact 파일 정보입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DependencyArtifactsFile {

    private String fileName;

    /**
     * 파일 사이즈. 바이트.
     *
     */
    private long size;

}
