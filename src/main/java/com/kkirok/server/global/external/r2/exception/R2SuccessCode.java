package com.kkirok.server.global.external.r2.exception;

import com.kkirok.server.global.common.exception.base.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum R2SuccessCode implements BaseSuccessCode {
    UPLOAD_SUCCESS(200, "파일 업로드 성공"),
    DOWNLOAD_PRESIGNED_URL_SUCCESS(200, "다운로드 Presigned URL 발급 성공");

    private final int status;
    private final String message;
}
