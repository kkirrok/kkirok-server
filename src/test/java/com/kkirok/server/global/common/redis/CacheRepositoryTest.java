package com.kkirok.server.global.common.redis;

import com.kkirok.server.global.common.redis.exception.RedisErrorCode;
import com.kkirok.server.global.common.redis.exception.RedisException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

@ExtendWith(MockitoExtension.class)
class CacheRepositoryTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private CacheRepository cacheRepository;

    @Test
    @DisplayName("get: 값이 있으면 Optional로 반환한다")
    void shouldReturnValue_whenKeyExists() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("key")).willReturn("value");

        Optional<String> result = cacheRepository.get("key");

        assertThat(result).contains("value");
    }

    @Test
    @DisplayName("get: 값이 없으면 빈 Optional을 반환한다")
    void shouldReturnEmpty_whenKeyDoesNotExist() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("key")).willReturn(null);

        Optional<String> result = cacheRepository.get("key");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("get: Redis 접근 실패 시 RedisException을 던진다")
    void shouldThrowRedisException_whenGetFails() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get("key")).willThrow(new RedisConnectionFailureException("fail"));

        assertThatThrownBy(() -> cacheRepository.get("key"))
                .isInstanceOf(RedisException.class)
                .extracting("baseErrorCode")
                .isEqualTo(RedisErrorCode.REDIS_READ_FAILED);
    }

    @Test
    @DisplayName("set: 값을 TTL과 함께 저장한다")
    void shouldSaveValueWithTtl() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);

        cacheRepository.set("key", "value", Duration.ofMinutes(1));

        then(valueOperations).should().set("key", "value", Duration.ofMinutes(1));
    }

    @Test
    @DisplayName("set: Redis 접근 실패 시 RedisException을 던진다")
    void shouldThrowRedisException_whenSetFails() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        willThrow(new RedisConnectionFailureException("fail"))
                .given(valueOperations).set(anyString(), anyString(), any(Duration.class));

        assertThatThrownBy(() -> cacheRepository.set("key", "value", Duration.ofMinutes(1)))
                .isInstanceOf(RedisException.class)
                .extracting("baseErrorCode")
                .isEqualTo(RedisErrorCode.REDIS_SAVE_FAILED);
    }

    @Test
    @DisplayName("delete: 키를 삭제한다")
    void shouldDeleteKey() {
        cacheRepository.delete("key");

        then(redisTemplate).should().delete("key");
    }

    @Test
    @DisplayName("delete: Redis 접근 실패 시 RedisException을 던진다")
    void shouldThrowRedisException_whenDeleteFails() {
        willThrow(new RedisConnectionFailureException("fail")).given(redisTemplate).delete("key");

        assertThatThrownBy(() -> cacheRepository.delete("key"))
                .isInstanceOf(RedisException.class)
                .extracting("baseErrorCode")
                .isEqualTo(RedisErrorCode.REDIS_DELETE_FAILED);
    }
}
