package io.teamcode.common.vcs.svn.callback;

import io.teamcode.common.vcs.svn.ChangedFile;
import io.teamcode.common.vcs.svn.ChangedFileAction;
import io.teamcode.common.vcs.svn.Commit;
import io.teamcode.service.project.integration.ProjectIntegrationServiceManager;
import org.apache.subversion.javahl.callback.LogMessageCallback;
import org.apache.subversion.javahl.types.ChangePath;
import org.apache.subversion.javahl.types.ChangePath.Action;
import org.apache.subversion.javahl.types.LogDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.*;

public class SvnLogMessageCallback implements LogMessageCallback {

    private static final Logger logger = LoggerFactory.getLogger(SvnLogMessageCallback.class);

	private List<Commit> repositoryHistories = new ArrayList<>();

    private Set<String> revisionProps;

    public SvnLogMessageCallback() {

    }

    public SvnLogMessageCallback(Set<String> revisionProps) {
        this.revisionProps = revisionProps;
    }

	public void singleMessage(Set<ChangePath> changedPaths, long revision, Map<String, byte[]> revprops, boolean hasChildren) {
		
		Commit history = new Commit();
		
		if (changedPaths != null) {
			for (ChangePath cp: changedPaths) {
                history.addChangedFile(new ChangedFile(ChangedFileAction.valueOf(cp.getAction().toString().toUpperCase()), cp.getPath(), cp.getNodeKind().toString()));
				
				if (cp.getAction().equals(Action.add)) {
                    history.plusAddtions();
				}
				else if (cp.getAction().equals(Action.delete)) {
                    history.plusDeletions();
				}
				else if (cp.getAction().equals(Action.replace)) {
                    history.plusReplacements();
				}
				else if (cp.getAction().equals(Action.modify)) {
                    history.plusModifications();
				}

                if (StringUtils.hasText(cp.getCopySrcPath())) {
                    history.setCopiedFromPath(cp.getCopySrcPath());
                    history.setCopiedFromRevision(cp.getCopySrcRevision());
                }
			}
		}

		String author;
		try {
			author = new String(revprops.get("svn:author"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			author = new String(revprops.get("svn:author"));
		}
		
		String message;
		try {
			message = new String(revprops.get("svn:log"), "UTF8");
		} catch (UnsupportedEncodingException e) {
			message = new String(revprops.get("svn:log"));
		}

		
		long timeMillis;
		try {
			LogDate date = new LogDate(new String(revprops.get("svn:date")));
			timeMillis = date.getDate().getTime();
		} catch (ParseException ex) {
			timeMillis = 0L;
		}

        if (this.revisionProps != null) {
            byte[] propValue;
            for (String revisionProp : revisionProps) {
            	logger.trace("'{}' 속성 값을 추출합니다...", revisionProp);
                propValue = revprops.get(revisionProp);
                if (propValue != null) {
                    try {
                        history.addRevisionProp(revisionProp, new String(propValue, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        history.addRevisionProp(revisionProp, new String(propValue));
                    }
                }
            }
        }

		if (revision != -1L) {
			history.setRevision(revision);
            history.setMessage(message);
            history.setAuthor(author);
            history.setCreatedAt(new Date(timeMillis));
			this.repositoryHistories.add(history);
		}
	}

	public List<Commit> getRepositoryHistories() {
		return this.repositoryHistories;
	}
}