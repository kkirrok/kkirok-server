package com.kkirok.server.domain.member.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record EmailVerificationStatusResponse(
        @Schema(description = "유저 이메일", example = "email@naver.com")
        String email,
        @Schema(description = "인증 성공 여부", example = "true")
        boolean verified
) {
    public static EmailVerificationStatusResponse of(final String email, final boolean verified) {
        return new EmailVerificationStatusResponse(email, verified);
    }
}
