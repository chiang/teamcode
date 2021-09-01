package io.teamcode.common.security.svn;

public class AuthzAccessRuleAlreadyExistsException extends AuthzException {

	private static final long serialVersionUID = -9105037813726046188L;
	
	public AuthzAccessRuleAlreadyExistsException() {
		super();
	}
	
	public AuthzAccessRuleAlreadyExistsException(String s) {
		super(s);
	}

}
