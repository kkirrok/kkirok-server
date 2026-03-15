package com.kkirok.server.domain.member.util;

import java.security.SecureRandom;

/**
 * 이메일 인증코드 생성
 * 랜덤 6자리 숫자 -> 문자열 반환
 */
public class EmailCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
