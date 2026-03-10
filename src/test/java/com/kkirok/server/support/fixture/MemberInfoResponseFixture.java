package com.kkirok.server.support.fixture;

import com.kkirok.server.domain.member.domain.SocialType;
import com.kkirok.server.global.auth.client.dto.MemberInfoResponse;

public final class MemberInfoResponseFixture {

    private MemberInfoResponseFixture() {
    }

    public static MemberInfoResponse createKakaoMember() {
        return MemberInfoResponse.of(
                1001L,
                "kakao-1001",
                "kkirok",
                "kkirok@test.com",
                SocialType.KAKAO
        );
    }

    public static MemberInfoResponse createNaverMember() {
        return MemberInfoResponse.of(
                2002L,
                "naver-2002",
                "kkirok",
                "kkirok@test.com",
                SocialType.NAVER
        );
    }

    public static MemberInfoResponse create(
            Long socialId,
            String providerUserId,
            String nickname,
            String email,
            SocialType socialType
    ) {
        return MemberInfoResponse.of(socialId, providerUserId, nickname, email, socialType);
    }
}
