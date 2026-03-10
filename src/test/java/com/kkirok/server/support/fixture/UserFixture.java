package com.kkirok.server.support.fixture;

import com.kkirok.server.domain.user.domain.Role;
import com.kkirok.server.domain.user.domain.Users;

public final class UserFixture {

    private UserFixture() {
    }

    public static Users create() {
        return Users.createWithRole(Role.MEMBER);
    }

    public static Users create(Role role) {
        return Users.createWithRole(role);
    }
}
