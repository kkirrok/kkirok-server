package com.kkirok.server.domain.member.domain;

public enum AuthProvider {
    LOCAL,
    KAKAO,
    NAVER;

    public static AuthProvider fromSocialType(SocialType socialType) {
        return switch (socialType) {
            case KAKAO -> KAKAO;
            case NAVER -> NAVER;
        };
    }
}
