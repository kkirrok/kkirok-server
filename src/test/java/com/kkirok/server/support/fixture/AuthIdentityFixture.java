package com.kkirok.server.support.fixture;

import com.kkirok.server.domain.member.domain.AuthIdentity;
import com.kkirok.server.domain.member.domain.AuthProvider;
import com.kkirok.server.domain.member.domain.Member;

public final class AuthIdentityFixture {

    private AuthIdentityFixture() {
    }

    public static AuthIdentity createLocal(Member member) {
        return AuthIdentity.createLocal(member, member.getEmail(), "encoded-password");
    }

    public static AuthIdentity createLocal(Member member, String email, String passwordHash) {
        return AuthIdentity.createLocal(member, email, passwordHash);
    }

    public static AuthIdentity createSocial(Member member, AuthProvider provider, String providerUserId) {
        return AuthIdentity.createSocial(member, provider, providerUserId);
    }
}
