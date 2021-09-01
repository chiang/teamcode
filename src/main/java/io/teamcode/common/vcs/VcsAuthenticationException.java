package io.teamcode.common.vcs;

public class VcsAuthenticationException extends RuntimeException {

	private static final long serialVersionUID = 1202801466253460154L;

	public VcsAuthenticationException() {
		//super(ErrorCode.SCM_SECURITY_ERROR);
		//this.setHttpStatus(HttpStatus.FORBIDDEN);

		//RestfulExceptionMessage restfulExceptionMessage = new RestfulExceptionMessage();
		//restfulExceptionMessage.setCategory(RestfulExceptionCategory.SCM);
		//restfulExceptionMessage.setMessage("subversion id or password is not matched. please contact administer.");
		//super.setRestfulExceptionMessage(restfulExceptionMessage);
		super();
	}

	public VcsAuthenticationException(String developerMessage) {
		/*super(ErrorCode.SCM_SECURITY_ERROR);
		this.setHttpStatus(HttpStatus.FORBIDDEN);
		
		RestfulExceptionMessage restfulExceptionMessage = new RestfulExceptionMessage();
		restfulExceptionMessage.setCategory(RestfulExceptionCategory.SCM);
		restfulExceptionMessage.setMessage("subversion id or password is not matched. please contact administer.");
		restfulExceptionMessage.setDeveloperMessage(developerMessage);
		super.setRestfulExceptionMessage(restfulExceptionMessage);*/
        super(developerMessage);
	}

}
