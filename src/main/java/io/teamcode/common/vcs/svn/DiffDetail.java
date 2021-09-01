package io.teamcode.common.vcs.svn;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chiang on 2017. 3. 25..
 */
@Data
public class DiffDetail {

    private List<String> headerLines = new ArrayList<>();

    private boolean binary;

    private String mimeTypeString;

    private String content;
}
