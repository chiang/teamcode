package io.teamcode.web.api.model.ci;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryInfo {

    private String url;

    private String revision;
}
