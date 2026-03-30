package com.kkirok.server.global.external.exception;

import com.kkirok.server.global.common.exception.KkirokException;

public class R2Exception extends KkirokException {
	public R2Exception(final ExternalErrorCode baseErrorCode) {
		super(baseErrorCode);
	}

	public R2Exception(final ExternalErrorCode baseErrorCode, final Throwable cause) {
		super(baseErrorCode, cause);
	}
}
