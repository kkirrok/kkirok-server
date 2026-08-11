package com.kkirok.server.global.auth.jwt.dao.redis;

import com.kkirok.server.domain.user.domain.Role;
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
public class RoleCacheRepository {

    private static final Duration ROLE_TTL = Duration.ofMinutes(1);
    private static final String ROLE_KEY_PREFIX = "auth:role:";

    private final RedisTemplate<String, String> redisTemplate;

    public Optional<Role> findRole(Long memberId) {
        try {
            String value = redisTemplate.opsForValue().get(roleKey(memberId));
            return Optional.ofNullable(value).map(Role::valueOf);
        } catch (DataAccessException exception) {
            log.error("Failed to read role cache. memberId={}", memberId, exception);
            throw new RedisException(RedisErrorCode.REDIS_READ_FAILED, exception);
        }
    }

    public void saveRole(Long memberId, Role role) {
        try {
            redisTemplate.opsForValue().set(roleKey(memberId), role.name(), ROLE_TTL);
        } catch (DataAccessException exception) {
            log.error("Failed to save role cache. memberId={}", memberId, exception);
            throw new RedisException(RedisErrorCode.REDIS_SAVE_FAILED, exception);
        }
    }

    public void deleteRole(Long memberId) {
        try {
            redisTemplate.delete(roleKey(memberId));
        } catch (DataAccessException exception) {
            log.error("Failed to delete role cache. memberId={}", memberId, exception);
            throw new RedisException(RedisErrorCode.REDIS_DELETE_FAILED, exception);
        }
    }

    private String roleKey(Long memberId) {
        return ROLE_KEY_PREFIX + memberId;
    }
}
