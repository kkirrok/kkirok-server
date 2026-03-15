package com.kkirok.server.domain.member.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailErrorCode implements BaseErrorCode {
    EMAIL_VERIFICATION_CODE_NOT_FOUND(400, "이메일 인증번호가 없거나 만료되었습니다."),
    EMAIL_VERIFICATION_CODE_MISMATCH(400, "이메일 인증번호가 일치하지 않습니다."),
    EMAIL_NOT_VERIFIED(400, "이메일 인증이 완료되지 않았습니다."),
    EMAIL_SEND_FAILED(500, "이메일 발송에 실패했습니다.");

    private final int status;
    private final String message;
}
