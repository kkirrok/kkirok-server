package com.kkirok.server.global.external.exception;

import com.kkirok.server.global.common.exception.KkirokException;

public class FastApiException extends KkirokException {

	public FastApiException(ExternalErrorCode errorCode) {
		super(errorCode);
	}

	public FastApiException(ExternalErrorCode errorCode, Throwable cause) {
		super(errorCode, cause);
	}
}
