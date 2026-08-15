package com.kkirok.server.global.common.redis;

import com.kkirok.server.global.common.redis.exception.RedisErrorCode;
import com.kkirok.server.global.common.redis.exception.RedisException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class CacheRepository {

    private final RedisTemplate<String, String> redisTemplate;

    public Optional<String> get(String key) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(key));
        } catch (DataAccessException exception) {
            log.error("Failed to read cache. key={}", key, exception);
            throw new RedisException(RedisErrorCode.REDIS_READ_FAILED, exception);
        }
    }

    public void set(String key, String value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (DataAccessException exception) {
            log.error("Failed to save cache. key={}", key, exception);
            throw new RedisException(RedisErrorCode.REDIS_SAVE_FAILED, exception);
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (DataAccessException exception) {
            log.error("Failed to delete cache. key={}", key, exception);
            throw new RedisException(RedisErrorCode.REDIS_DELETE_FAILED, exception);
        }
    }
}
