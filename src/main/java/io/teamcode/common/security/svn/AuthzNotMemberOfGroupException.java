package io.teamcode.common.security.svn;

public class AuthzNotMemberOfGroupException extends AuthzException {

	private static final long serialVersionUID = -3398501763495509901L;
	
	public AuthzNotMemberOfGroupException() {
		super();
	}
	
	public AuthzNotMemberOfGroupException(String s) {
		super(s);
	}

}
