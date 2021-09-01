package io.teamcode.common.vcs.svn.callback;

import org.apache.subversion.javahl.CommitInfo;
import org.apache.subversion.javahl.callback.CommitCallback;

public class SvnCommitCallback implements CommitCallback {

	@Override
	public void commitInfo(CommitInfo info) {
		//System.out.println("-----------------commitinfo: " + info.getPostCommitError());
	}

}
