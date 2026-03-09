package com.kkirok.server.support.fixture;

import com.kkirok.server.domain.member.application.dto.request.LocalSignUpRequest;

public final class LocalSignUpRequestFixture {

    private LocalSignUpRequestFixture() {
    }

    public static LocalSignUpRequest create() {
        return new LocalSignUpRequest(
                "kkirok@test.com",
                "kkirok",
                "password123!"
        );
    }

    public static LocalSignUpRequest create(String email, String nickname, String password) {
        return new LocalSignUpRequest(email, nickname, password);
    }
}
