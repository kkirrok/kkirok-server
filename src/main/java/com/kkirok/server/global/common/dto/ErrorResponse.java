package com.kkirok.server.global.common.dto;

import com.kkirok.server.global.common.exception.KkirokException;
import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(
	@Schema(description = "에러 코드(enum 상수명 또는 시스템 코드)")
	String code,
	@Schema(description = "HTTP 상태 코드")
	int status,
	@Schema(description = "에러 메시지")
	String message
) {
	public static ErrorResponse of(final int status, final String message) {
		return new ErrorResponse("UNDEFINED_ERROR", status, message);
	}

	public static ErrorResponse of(final String code, final int status, final String message) {
		return new ErrorResponse(code, status, message);
	}

	public static ErrorResponse from(final BaseErrorCode baseErrorCode) {
		return new ErrorResponse(extractCode(baseErrorCode), baseErrorCode.getStatus(), baseErrorCode.getMessage());
	}

	public static ErrorResponse from(final KkirokException exception) {
		BaseErrorCode baseErrorCode = exception.getBaseErrorCode();
		String message = baseErrorCode.getMessage();
		String additionalException = exception.getAdditionalException();

		if (additionalException != null && !additionalException.isBlank()) {
			message = message + " - " + additionalException;
		}

		return new ErrorResponse(extractCode(baseErrorCode), baseErrorCode.getStatus(), message);
	}

	private static String extractCode(BaseErrorCode baseErrorCode) {
		return (baseErrorCode instanceof Enum<?> enumCode)
			? enumCode.name()
			: baseErrorCode.getClass().getSimpleName();
	}
}
