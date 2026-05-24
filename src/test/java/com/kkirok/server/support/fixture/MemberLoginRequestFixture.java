package com.kkirok.server.support.fixture;

import com.kkirok.server.domain.member.domain.SocialType;
import com.kkirok.server.global.auth.client.dto.MemberLoginRequest;

public final class MemberLoginRequestFixture {

    private MemberLoginRequestFixture() {
    }

    public static MemberLoginRequest createKakao() {
        return new MemberLoginRequest(SocialType.KAKAO, "kakao-access-token");
    }

    public static MemberLoginRequest createNaver() {
        return new MemberLoginRequest(SocialType.NAVER, "naver-access-token");
    }

    public static MemberLoginRequest create(SocialType socialType, String accessToken) {
        return new MemberLoginRequest(socialType, accessToken);
    }
}
