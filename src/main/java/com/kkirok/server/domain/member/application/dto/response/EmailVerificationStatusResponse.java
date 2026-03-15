package com.kkirok.server.domain.member.application.dto.response;

public record EmailVerificationStatusResponse(
        String email,
        boolean verified
) {
    public static EmailVerificationStatusResponse of(final String email, final boolean verified) {
        return new EmailVerificationStatusResponse(email, verified);
    }
}
