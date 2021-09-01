package io.teamcode.common.vcs.svn;

import org.apache.subversion.javahl.ISVNClient;
import org.apache.subversion.javahl.SVNClient;
import org.springframework.stereotype.Component;

@Component
public class SvnClientFactory {
	
	public ISVNClient createLocalClient() {
		return this.createLocalClient("teamcode", "");
	}

    public ISVNClient createLocalClient(final String user, final String password) {
        ISVNClient client = new SVNClient();
        client.username(user);
        client.password(password);

        return client;
    }

}
