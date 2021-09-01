package io.teamcode.web.api.model.ci;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chiang on 2017. 7. 31..
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dependency {

    private long id;

    private String token;

    private String name;

    private DependencyArtifactsFile artifactsFile;
}
