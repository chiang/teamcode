package io.teamcode.service.vcs;

import io.teamcode.TeamcodeException;

public class SourceRepositoryCreationException extends TeamcodeException {

	private static final long serialVersionUID = 7742397517001600263L;
	
	public SourceRepositoryCreationException(String message) {
		super(message);
	}
	
	public SourceRepositoryCreationException(String message, Throwable t) {
		super(message, t);
	}

}
