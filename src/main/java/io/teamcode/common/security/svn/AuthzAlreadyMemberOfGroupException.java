package io.teamcode.common.security.svn;

public class AuthzAlreadyMemberOfGroupException extends AuthzException {

	private static final long serialVersionUID = -6373230285889066744L;
	
	public AuthzAlreadyMemberOfGroupException() {
		super();
	}
	
	public AuthzAlreadyMemberOfGroupException(String s) {
		super(s);
	}

}
