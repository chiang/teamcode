package io.teamcode.common.security.svn;

public class AuthzGroupMemberAlreadyExistsException extends AuthzException {
	
	private static final long serialVersionUID = 459489339789691308L;
	
	public AuthzGroupMemberAlreadyExistsException() {
		super();
	}
	
	public AuthzGroupMemberAlreadyExistsException(String s) {
		super(s);
	}

}
