package io.teamcode.common.vcs;

import lombok.Data;
import lombok.ToString;

/**
 * Created by chiang on 2015. 10. 26..
 */
@Data
@ToString
public class SourceBrowserBreadcrumb {

    private String name;

    private String path;

    private boolean last;

    public SourceBrowserBreadcrumb(String name, String path, boolean last) {
        this.name = name;
        this.path = path;
        this.last = last;
    }
}
