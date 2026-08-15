package com.kkirok.server.global.auth.jwt.application;

import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.global.common.redis.CacheRepository;
import com.kkirok.server.global.common.redis.exception.RedisException;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActiveCheckCacheService {

    private static final String CACHE_LOOKUP_METRIC = "cache.lookup";
    private static final String CACHE_NAME = "active_check";
    private static final String ACTIVE_KEY_PREFIX = "auth:active:";
    private static final String ACTIVE_VALUE = "true";
    private static final Duration ACTIVE_TTL = Duration.ofMinutes(1);

    private final CacheRepository cacheRepository;
    private final MemberUseCase memberUseCase;
    private final MeterRegistry meterRegistry;

    @Transactional(readOnly = true)
    public void checkActive(Long memberId) {
        if (readCache(memberId)) {
            meterRegistry.counter(CACHE_LOOKUP_METRIC, "cache", CACHE_NAME, "result", "hit").increment();
            return;
        }

        meterRegistry.counter(CACHE_LOOKUP_METRIC, "cache", CACHE_NAME, "result", "miss").increment();
        memberUseCase.findMemberByMemberId(memberId);
        writeCache(memberId);
    }

    public void evictActive(Long memberId) {
        try {
            cacheRepository.delete(activeKey(memberId));
        } catch (RedisException exception) {
            log.warn("Failed to evict active-check cache, stale status may be served until TTL expires. memberId={}", memberId, exception);
        }
    }

    private boolean readCache(Long memberId) {
        try {
            return cacheRepository.get(activeKey(memberId)).isPresent();
        } catch (RedisException exception) {
            log.warn("Failed to read active-check cache, falling back to DB. memberId={}", memberId, exception);
            return false;
        }
    }

    private void writeCache(Long memberId) {
        try {
            cacheRepository.set(activeKey(memberId), ACTIVE_VALUE, ACTIVE_TTL);
        } catch (RedisException exception) {
            log.warn("Failed to write active-check cache, ignoring. memberId={}", memberId, exception);
        }
    }

    private String activeKey(Long memberId) {
        return ACTIVE_KEY_PREFIX + memberId;
    }
}
