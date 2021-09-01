package io.teamcode.common.vcs.svn.callback;

import org.apache.subversion.javahl.callback.LogMessageCallback;
import org.apache.subversion.javahl.types.ChangePath;
import org.apache.subversion.javahl.types.LogDate;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * 정말 간단히게, 작성자와 메시지, 날짜만 가져옵니다.
 */
public class SimpleLogMessageCallback implements LogMessageCallback {

    private SimpleLogMessage simpleLogMessage;


    @Override
    public void singleMessage(Set<ChangePath> set, long revision, Map<String, byte[]> revprops, boolean b) {
        this.simpleLogMessage = new SimpleLogMessage();

        this.simpleLogMessage.setRevisionNumber(revision);

        try {
            this.simpleLogMessage.setAuthor(new String(revprops.get("svn:author"), "UTF8"));
        } catch (UnsupportedEncodingException e) {
            this.simpleLogMessage.setAuthor(new String(revprops.get("svn:author")));
        }

        try {
            this.simpleLogMessage.setMessage(new String((byte[]) revprops.get("svn:log"),
                    "UTF8"));
        } catch (UnsupportedEncodingException e) {
            this.simpleLogMessage.setMessage(new String((byte[]) revprops.get("svn:log")));
        }

        try {
            LogDate date = new LogDate(new String(revprops.get("svn:date")));
            this.simpleLogMessage.setCommitedAt(date.getDate());
        } catch (ParseException ex) {
            //TODO logging?
        }
    }

    public SimpleLogMessage getSimpleLogMessage() {
        return this.simpleLogMessage;
    }
}
