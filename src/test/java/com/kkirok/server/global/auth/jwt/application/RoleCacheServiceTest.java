package com.kkirok.server.global.auth.jwt.application;

import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.global.common.redis.CacheRepository;
import com.kkirok.server.support.fixture.MemberFixture;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RoleCacheServiceTest {

    @Mock
    private CacheRepository cacheRepository;

    @Mock
    private MemberUseCase memberUseCase;

    private SimpleMeterRegistry meterRegistry;
    private RoleCacheService roleCacheService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        roleCacheService = new RoleCacheService(cacheRepository, memberUseCase, meterRegistry);
    }

    @Test
    @DisplayName("캐시 히트 시 cache.lookup 카운터의 result=hit 태그가 증가한다")
    void shouldIncrementHitCounter_whenCacheHit() {
        // Given
        given(cacheRepository.get("auth:role:1")).willReturn(Optional.of("USER"));

        // When
        roleCacheService.getRole(1L);

        // Then
        double hitCount = meterRegistry.get("cache.lookup")
                .tag("cache", "role_cache")
                .tag("result", "hit")
                .counter()
                .count();
        assertThat(hitCount).isEqualTo(1.0);
    }

    @Test
    @DisplayName("캐시 미스 시 cache.lookup 카운터의 result=miss 태그가 증가한다")
    void shouldIncrementMissCounter_whenCacheMiss() {
        // Given
        Member member = MemberFixture.createLocalMember();
        given(cacheRepository.get("auth:role:1")).willReturn(Optional.empty());
        given(memberUseCase.findMemberByMemberId(1L)).willReturn(member);

        // When
        roleCacheService.getRole(1L);

        // Then
        double missCount = meterRegistry.get("cache.lookup")
                .tag("cache", "role_cache")
                .tag("result", "miss")
                .counter()
                .count();
        assertThat(missCount).isEqualTo(1.0);
    }
}
