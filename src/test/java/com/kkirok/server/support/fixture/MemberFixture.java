package com.kkirok.server.support.fixture;

import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.member.domain.SocialType;
import com.kkirok.server.domain.user.domain.Users;

public final class MemberFixture {

    private MemberFixture() {
    }

    public static Member createLocalMember() {
        Users user = UserFixture.create();
        return Member.createLocal("kkirok@test.com", user);
    }

    public static Member createLocalMember(String nickname, String email) {
        Users user = UserFixture.create();
        return Member.createLocal(email, user);
    }

    public static Member createLocalMember(String nickname, String email, Users user) {
        return Member.createLocal(email, user);
    }

    public static Member createSocialMember(Long socialId, SocialType socialType) {
        Users user = UserFixture.create();
        return Member.create(
                MemberInfoResponseFixture.create(socialId, "provider-user-id", "kkirok", "kkirok@test.com", socialType),
                user
        );
    }

    public static Member createSocialMember(
            String nickname,
            String email,
            Users user,
            Long socialId,
            SocialType socialType
    ) {
        return Member.create(
                MemberInfoResponseFixture.create(socialId, "provider-user-id", nickname, email, socialType),
                user
        );
    }
}
