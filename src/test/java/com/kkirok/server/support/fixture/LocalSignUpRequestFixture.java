package com.kkirok.server.support.fixture;

import com.kkirok.server.domain.member.application.dto.request.LocalSignUpRequest;

public final class LocalSignUpRequestFixture {

    private LocalSignUpRequestFixture() {
    }

    public static LocalSignUpRequest create() {
        return new LocalSignUpRequest(
                "kkirok@test.com",
                "password123!"
        );
    }

    public static LocalSignUpRequest create(String email, String password) {
        return new LocalSignUpRequest(email, password);
    }
}
