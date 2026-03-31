package com.kkirok.server.global.external.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExternalErrorCode implements BaseErrorCode {

	// OpenAI
	OPENAI_CLIENT_ERROR(502, "OpenAI 호출에 실패했습니다."),

	// FastAPI
	FAST_API_CLIENT_ERROR(502, "FastAPI 호출에 실패했습니다."),
	FAST_API_INVALID_REQUEST(400, "FastAPI 요청 정보가 유효하지 않습니다."),

	// R2
	R2_INVALID_FILE_REQUEST(400, "업로드할 파일이 유효하지 않습니다."),
	R2_INVALID_OBJECT_KEY(400, "R2 객체 키가 유효하지 않습니다."),
	R2_FILE_STREAM_READ_FAILED(500, "업로드 파일을 읽는 중 오류가 발생했습니다."),
	R2_FILE_UPLOAD_FAILED(500, "파일 업로드에 실패했습니다."),
	R2_PRESIGNED_URL_GENERATION_FAILED(500, "Presigned URL 생성에 실패했습니다.");

	private final int status;
	private final String message;

}
