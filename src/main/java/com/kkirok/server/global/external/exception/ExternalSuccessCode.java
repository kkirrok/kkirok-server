package com.kkirok.server.global.external.exception;

import com.kkirok.server.global.common.exception.base.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExternalSuccessCode implements BaseSuccessCode {

	// OpenAI
	OPENAI_MEAL_ANALYSIS_SUCCESS(200, "OpenAI 식단 분석 성공"),

	// R2
	R2_UPLOAD_SUCCESS(200, "파일 업로드 성공"),
	R2_DOWNLOAD_PRESIGNED_URL_SUCCESS(200, "다운로드 Presigned URL 발급 성공");

	private final int status;
	private final String message;
}
