package com.kkirok.server.global.external.exception;

import com.kkirok.server.global.common.exception.KkirokException;

public class OpenAiException extends KkirokException {

	public OpenAiException(ExternalErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}

}
