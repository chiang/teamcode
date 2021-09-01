package io.teamcode.common.vcs.svn.callback;

import io.teamcode.common.TeamcodeConstants;
import io.teamcode.common.vcs.svn.ChangedFile;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chiang on 2017. 4. 9..
 */
@Data
public class SummaryLogMessage {

    private String author;

    private String message;

    private Date committedAt;

    private int commits;

    //TODO 너무 많은 수의 아이템이 오면 다 안 보여주는 것으로 하기...
    private boolean maxExceed;

    private List<ChangedFile> changedFiles = new ArrayList<>();

    public void addChangedFile(ChangedFile changedFile) {
        this.changedFiles.add(changedFile);
    }

    public boolean isSkipCi() {
        Pattern p = Pattern.compile(TeamcodeConstants.DEFAULT_SKIP_CI_REGEXP);
        Matcher m = p.matcher(message);

        return m.find();
    }
}
