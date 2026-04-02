package com.kkirok.server.global.common.exception;


import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import lombok.Getter;

@Getter
public class KkirokException extends RuntimeException {
	private final BaseErrorCode baseErrorCode;
	private final String additionalException;

	public KkirokException(BaseErrorCode baseErrorCode) {
		super(baseErrorCode.getMessage());
		this.baseErrorCode = baseErrorCode;
		this.additionalException = null;
	}

	public KkirokException(BaseErrorCode baseErrorCode, Throwable cause) {
		super(baseErrorCode.getMessage(), cause);
		this.baseErrorCode = baseErrorCode;
		this.additionalException = null;
	}

	public KkirokException(BaseErrorCode baseErrorCode, String additionalMessage) {
		super(appendAdditionalMessage(baseErrorCode.getMessage(), additionalMessage));
		this.baseErrorCode = baseErrorCode;
		this.additionalException = additionalMessage;
	}

	private static String appendAdditionalMessage(String baseMessage, String additionalException) {
		if (additionalException == null || additionalException.isBlank()) {
			return baseMessage;
		}
		return baseMessage + " - " + additionalException;
	}
}
