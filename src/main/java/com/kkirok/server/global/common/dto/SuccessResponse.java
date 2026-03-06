package com.kkirok.server.global.common.dto;

import com.kkirok.server.global.common.exception.base.BaseSuccessCode;
import io.swagger.v3.oas.annotations.media.Schema;

public record SuccessResponse<T>(
	@Schema(description = "성공 코드(enum 상수명)")
	String code,
	@Schema(description = "HTTP 상태 코드")
	int status,
	@Schema(description = "응답 메시지")
	String message,
	@Schema(description = "응답 데이터")
	T data
) {
	public static <T> SuccessResponse<T> of(final BaseSuccessCode baseSuccessCode, final T data) {
		return new SuccessResponse<>(extractCode(baseSuccessCode), baseSuccessCode.getStatus(), baseSuccessCode.getMessage(), data);
	}

	public static <T> SuccessResponse<T> from(final BaseSuccessCode baseSuccessCode) {
		return new SuccessResponse<>(extractCode(baseSuccessCode), baseSuccessCode.getStatus(), baseSuccessCode.getMessage(), null);
	}

	private static String extractCode(BaseSuccessCode baseSuccessCode) {
		return (baseSuccessCode instanceof Enum<?> enumCode)
			? enumCode.name()
			: baseSuccessCode.getClass().getSimpleName();
	}
}
