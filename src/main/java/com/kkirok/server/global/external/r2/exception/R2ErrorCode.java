package com.kkirok.server.global.external.r2.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum R2ErrorCode implements BaseErrorCode {
    INVALID_FILE_REQUEST(400, "업로드할 파일이 유효하지 않습니다."),
    INVALID_OBJECT_KEY(400, "R2 객체 키가 유효하지 않습니다."),
    FILE_STREAM_READ_FAILED(500, "업로드 파일을 읽는 중 오류가 발생했습니다."),
    FILE_UPLOAD_FAILED(500, "파일 업로드에 실패했습니다."),
    PRESIGNED_URL_GENERATION_FAILED(500, "Presigned URL 생성에 실패했습니다.");

    private final int status;
    private final String message;
}
