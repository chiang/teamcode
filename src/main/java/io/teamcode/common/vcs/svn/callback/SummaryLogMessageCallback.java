package io.teamcode.common.vcs.svn.callback;

import io.teamcode.common.vcs.svn.ChangedFile;
import io.teamcode.common.vcs.svn.ChangedFileAction;
import org.apache.subversion.javahl.callback.LogMessageCallback;
import org.apache.subversion.javahl.types.ChangePath;
import org.apache.subversion.javahl.types.LogDate;
import org.apache.subversion.javahl.types.NodeKind;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Map;
import java.util.Set;

/**
 * Created by chiang on 2017. 4. 9..
 */
public class SummaryLogMessageCallback implements LogMessageCallback {

    private SummaryLogMessage summaryLogMessage;


    @Override
    public void singleMessage(Set<ChangePath> changedPaths, long l, Map<String, byte[]> revprops, boolean b) {
        this.summaryLogMessage = new SummaryLogMessage();

        try {
            this.summaryLogMessage.setAuthor(new String(revprops.get("svn:author"), "UTF8"));
        } catch (UnsupportedEncodingException e) {
            this.summaryLogMessage.setAuthor(new String(revprops.get("svn:author")));
        }

        try {
            this.summaryLogMessage.setMessage(new String((byte[]) revprops.get("svn:log"),
                    "UTF8"));
        } catch (UnsupportedEncodingException e) {
            this.summaryLogMessage.setMessage(new String((byte[]) revprops.get("svn:log")));
        }

        try {
            LogDate date = new LogDate(new String(revprops.get("svn:date")));
            this.summaryLogMessage.setCommittedAt(date.getDate());
        } catch (ParseException ex) {
            //TODO logging?
        }

        if (changedPaths != null) {
            this.summaryLogMessage.setCommits(changedPaths.size());

            ChangedFile changedFile;
            int max = 100;
            int count = 0;
            for (ChangePath cp: changedPaths) {
                count++;
                if (count >= max) {
                    summaryLogMessage.setMaxExceed(true);
                    break;
                }

                //TODO 일단 파일만 처리?
                if (cp.getNodeKind() == NodeKind.file) {
                    changedFile = new ChangedFile(ChangedFileAction.valueOf(cp.getAction().toString().toUpperCase()), cp.getPath(), cp.getNodeKind().toString());
                    this.summaryLogMessage.addChangedFile(changedFile);
                }
            }
        }
    }

    public SummaryLogMessage getSummaryLogMessage() {
        return this.summaryLogMessage;
    }
}
