package com.kkirok.server.domain.member.dao.redis;

import com.kkirok.server.global.common.redis.exception.RedisErrorCode;
import com.kkirok.server.global.common.redis.exception.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis로 이메일 인증 정보를 관리합니다.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EmailVerificationRepository {

    private static final Duration VERIFICATION_CODE_TTL = Duration.ofMinutes(10);
    private static final Duration VERIFIED_EMAIL_TTL = Duration.ofMinutes(30);
    private static final String VERIFICATION_CODE_KEY = "email:verification:code:";
    private static final String VERIFIED_EMAIL_KEY = "email:verification:verified:";
    private static final String VERIFIED_VALUE = "true";

    private final RedisTemplate<String, String> redisTemplate;

    // 인증코드 저장
    public void saveVerificationCode(String email, String code) {
        try {
            redisTemplate.opsForValue().set(codeKey(email), code, VERIFICATION_CODE_TTL);
        } catch (DataAccessException exception) {
            log.error("Failed to save email verification code. email={}", email, exception);
            throw new RedisException(RedisErrorCode.REDIS_SAVE_FAILED, exception);
        }
    }

    // 인증코드 조회
    public Optional<String> getVerificationCode(String email) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(codeKey(email)));
        } catch (DataAccessException exception) {
            log.error("Failed to read email verification code. email={}", email, exception);
            throw new RedisException(RedisErrorCode.REDIS_READ_FAILED, exception);
        }
    }

    // 인증코드 제거
    public void deleteVerificationCode(String email) {
        delete(codeKey(email), email);
    }

    // 인증완료 처리
    public void markVerified(String email) {
        try {
            redisTemplate.opsForValue().set(verifiedKey(email), VERIFIED_VALUE, VERIFIED_EMAIL_TTL);
        } catch (DataAccessException exception) {
            log.error("Failed to save verified email marker. email={}", email, exception);
            throw new RedisException(RedisErrorCode.REDIS_SAVE_FAILED, exception);
        }
    }

    // 인증 여부 조회
    public boolean isVerified(String email) {
        try {
            return redisTemplate.hasKey(verifiedKey(email));
        } catch (DataAccessException exception) {
            log.error("Failed to read verified email marker. email={}", email, exception);
            throw new RedisException(RedisErrorCode.REDIS_READ_FAILED, exception);
        }
    }

    // 인증 여부 삭제
    public void deleteVerified(String email) {
        delete(verifiedKey(email), email);
    }

    public static Duration verificationCodeTtl() {
        return VERIFICATION_CODE_TTL;
    }

    private String codeKey(String email) {
        return VERIFICATION_CODE_KEY + email;
    }

    private String verifiedKey(String email) {
        return VERIFIED_EMAIL_KEY + email;
    }

    private void delete(String key, String email) {
        try {
            redisTemplate.delete(key);
        } catch (DataAccessException exception) {
            log.error("Failed to delete redis data. email={}, key={}", email, key, exception);
            throw new RedisException(RedisErrorCode.REDIS_DELETE_FAILED, exception);
        }
    }
}
