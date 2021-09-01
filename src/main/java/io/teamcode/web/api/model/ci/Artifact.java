package io.teamcode.web.api.model.ci;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Created by chiang on 2017. 7. 30..
 */
@Data
public class Artifact {

    /**
     * Artifact 파일 이름
     *
     */
    private String name;

    private List<String> paths;

    public List<String> getPaths() {
        if (this.paths == null)
            return Collections.emptyList();
        else
            return this.paths;
    }

}
