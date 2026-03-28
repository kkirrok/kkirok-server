package com.kkirok.server.support.fixture;

import com.kkirok.server.domain.member.application.dto.request.ResetPasswordRequest;

public final class ResetPasswordRequestFixture {

    private ResetPasswordRequestFixture() {
    }

    public static ResetPasswordRequest create() {
        return new ResetPasswordRequest(
                "kkirok@test.com",
                "김준용",
                "newPassword123!"
        );
    }
}
