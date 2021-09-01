package io.teamcode.domain.api;

import io.teamcode.common.Strings;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * Created by chiang on 2017. 3. 22..
 */
@Data
@Builder
public class ChangedFile {

    private String path;

    private String author;

    private Date lastModifiedAt;

    public String getName() {

        return Strings.getFileNameFromPath(this.getPath());
    }

}
