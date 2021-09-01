package io.teamcode.common.vcs.svn.callback;

import lombok.Data;

import java.util.Date;

/**
 * Created by chiang on 2017. 4. 9..
 */
@Data
public class SimpleLogMessage {

    private long revisionNumber;

    private String author;

    private String message;

    private Date commitedAt;
}
