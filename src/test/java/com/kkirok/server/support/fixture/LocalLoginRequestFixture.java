package com.kkirok.server.support.fixture;

import com.kkirok.server.domain.member.application.dto.request.LocalLoginRequest;

public final class LocalLoginRequestFixture {

    private LocalLoginRequestFixture() {
    }

    public static LocalLoginRequest create() {
        return new LocalLoginRequest(
                "kkirok@test.com",
                "password123!"
        );
    }

    public static LocalLoginRequest create(String email, String password) {
        return new LocalLoginRequest(email, password);
    }
}
