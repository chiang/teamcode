package io.teamcode.common.vcs.svn.callback;

import org.apache.subversion.javahl.CommitItem;
import org.apache.subversion.javahl.callback.CommitMessageCallback;

import java.util.Set;

/**
 * 이 Callback을 전달해야 commit log가 전송이 된다. 보통 이 callback을 필요로 하는 svn command에서는 
 * 이 callback이 null로 전달이 되면 해당 command가 실행이 되지 않는다.
 * 
 * @author chiang
 *
 */
public class SvnCommitMessageCallback implements CommitMessageCallback {
	
	private String commitMessage;
	
	public SvnCommitMessageCallback(String commitMessage) {
		this.commitMessage = commitMessage;
	}

	@Override
	public String getLogMessage(Set<CommitItem> elementsToBeCommitted) {
			
		// TODO 위의 것이랑 정리해서 처리.
		return commitMessage;
	}

}
