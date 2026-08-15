package com.kkirok.server.global.auth.jwt.application;

import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.user.domain.Role;
import com.kkirok.server.global.auth.jwt.dao.redis.RoleCacheRepository;
import com.kkirok.server.global.common.redis.exception.RedisException;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleCacheService {

    private static final String CACHE_LOOKUP_METRIC = "cache.lookup";
    private static final String CACHE_NAME = "role_cache";

    private final RoleCacheRepository roleCacheRepository;
    private final MemberUseCase memberUseCase;
    private final MeterRegistry meterRegistry;

    @Transactional(readOnly = true)
    public Role getRole(Long memberId) {
        Optional<Role> cached = readCache(memberId);
        if (cached.isPresent()) {
            meterRegistry.counter(CACHE_LOOKUP_METRIC, "cache", CACHE_NAME, "result", "hit").increment();
            return cached.get();
        }

        meterRegistry.counter(CACHE_LOOKUP_METRIC, "cache", CACHE_NAME, "result", "miss").increment();
        Role role = memberUseCase.findMemberByMemberId(memberId).getUser().getRole();
        writeCache(memberId, role);
        return role;
    }

    public void evictRole(Long memberId) {
        try {
            roleCacheRepository.deleteRole(memberId);
        } catch (RedisException exception) {
            log.warn("Failed to evict role cache, stale role may be served until TTL expires. memberId={}", memberId, exception);
        }
    }

    private Optional<Role> readCache(Long memberId) {
        try {
            return roleCacheRepository.findRole(memberId);
        } catch (RedisException exception) {
            log.warn("Failed to read role cache, falling back to DB. memberId={}", memberId, exception);
            return Optional.empty();
        }
    }

    private void writeCache(Long memberId, Role role) {
        try {
            roleCacheRepository.saveRole(memberId, role);
        } catch (RedisException exception) {
            log.warn("Failed to write role cache, ignoring. memberId={}", memberId, exception);
        }
    }
}
