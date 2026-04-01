package com.kkirok.server.global.external.exception;

import com.kkirok.server.global.common.exception.KkirokException;

public class PromptException extends KkirokException {

	public PromptException(ExternalErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}
