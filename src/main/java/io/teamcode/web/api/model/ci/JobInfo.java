package io.teamcode.web.api.model.ci;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chiang on 2017. 5. 11..
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobInfo {

    private String name;

    private String stage;

    private Long projectId;

    private String projectName;

    /*
    Name        string `json:"name"`
	Stage       string `json:"stage"`
	ProjectID   int    `json:"project_id"`
	ProjectName string `json:"project_name"`
     */
}
