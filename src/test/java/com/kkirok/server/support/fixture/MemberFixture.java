package com.kkirok.server.support.fixture;

import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.member.domain.SocialType;
import com.kkirok.server.domain.user.domain.Users;
import org.springframework.test.util.ReflectionTestUtils;

public final class MemberFixture {

    private MemberFixture() {
    }

    public static Member createLocalMember() {
        Users user = UserFixture.create();
        return Member.createLocal("kkirok@test.com", user);
    }

    public static Member createLocalMember(String nickname, String email) {
        Users user = UserFixture.create();
        Member member = Member.createLocal(email, user);
        ReflectionTestUtils.setField(member, "nickname", nickname);
        ReflectionTestUtils.setField(member, "name", nickname);
        return member;
    }

    public static Member createLocalMember(String nickname, String email, Users user) {
        Member member = Member.createLocal(email, user);
        ReflectionTestUtils.setField(member, "nickname", nickname);
        ReflectionTestUtils.setField(member, "name", nickname);
        return member;
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
