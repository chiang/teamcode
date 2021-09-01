package io.teamcode.common.vcs.svn;

import io.teamcode.common.vcs.VcsAuthenticationException;
import io.teamcode.common.vcs.VcsCommunicationFailureException;
import io.teamcode.service.ResourceNotFoundException;
import org.apache.subversion.javahl.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SvnExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(SvnExceptionHandler.class);

	private static final int ERROR_CODE_FILE_NOTFOUND = 160013;

	public static final boolean isFileNotFoundError(ClientException e) {
		return e.getAprError() == ERROR_CODE_FILE_NOTFOUND;
	}
	
	public static final void throwDetailSvnException(ClientException e) {
		logger.trace("JavaHL ClientException error code: [{}], error message: [{}]", e.getAprError(), e.getMessage());
		
		if (e.getAprError() == 170001 || e.getAprError() == 215004) {//TODO e.getMessage 전달
			logger.error("cannot authentcate to subversion. message [{}]", e.getMessage());
			throw new VcsAuthenticationException(e.getMessage());
		}
		else if (e.getAprError() == 730061) {
			throw new VcsAuthenticationException();
		}
		else if (e.getAprError() == ERROR_CODE_FILE_NOTFOUND) {
			throw new ResourceNotFoundException("서브버전에서 요청받은 파일을 찾을 수 없습니다.");
		}
		//TODO
		else {
			throw new VcsCommunicationFailureException(e.getMessage(), e);
		}
	}

}
