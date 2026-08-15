package com.kkirok.server.global.auth.jwt.application;

import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.global.common.exception.NotFoundException;
import com.kkirok.server.global.common.redis.CacheRepository;
import com.kkirok.server.support.fixture.MemberFixture;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ActiveCheckCacheServiceTest {

    @Mock
    private CacheRepository cacheRepository;

    @Mock
    private MemberUseCase memberUseCase;

    private SimpleMeterRegistry meterRegistry;
    private ActiveCheckCacheService activeCheckCacheService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        activeCheckCacheService = new ActiveCheckCacheService(cacheRepository, memberUseCase, meterRegistry);
    }

    @Test
    @DisplayName("캐시 히트 시 DB를 조회하지 않고 hit 카운터가 증가한다")
    void shouldSkipDbAndIncrementHitCounter_whenCacheHit() {
        // Given
        given(cacheRepository.get("auth:active:1")).willReturn(Optional.of("true"));

        // When
        activeCheckCacheService.checkActive(1L);

        // Then
        then(memberUseCase).shouldHaveNoInteractions();
        double hitCount = meterRegistry.get("cache.lookup")
                .tag("cache", "active_check")
                .tag("result", "hit")
                .counter()
                .count();
        assertThat(hitCount).isEqualTo(1.0);
    }

    @Test
    @DisplayName("캐시 미스 + 활성 회원이면 DB 조회 후 캐시에 기록하고 miss 카운터가 증가한다")
    void shouldQueryDbAndWriteCache_whenCacheMissAndMemberActive() {
        // Given
        Member member = MemberFixture.createLocalMember();
        given(cacheRepository.get("auth:active:1")).willReturn(Optional.empty());
        given(memberUseCase.findMemberByMemberId(1L)).willReturn(member);

        // When
        assertThatCode(() -> activeCheckCacheService.checkActive(1L)).doesNotThrowAnyException();

        // Then
        then(cacheRepository).should().set("auth:active:1", "true", Duration.ofMinutes(1));
        double missCount = meterRegistry.get("cache.lookup")
                .tag("cache", "active_check")
                .tag("result", "miss")
                .counter()
                .count();
        assertThat(missCount).isEqualTo(1.0);
    }

    @Test
    @DisplayName("캐시 미스 + 탈퇴 회원이면 예외가 전파되고 캐시에 기록하지 않는다")
    void shouldThrowAndNotWriteCache_whenCacheMissAndMemberQuit() {
        // Given
        given(cacheRepository.get("auth:active:1")).willReturn(Optional.empty());
        given(memberUseCase.findMemberByMemberId(1L))
                .willThrow(new NotFoundException(MemberErrorCode.DELETED_MEMBER));

        // When, Then
        assertThatThrownBy(() -> activeCheckCacheService.checkActive(1L))
                .isInstanceOf(NotFoundException.class)
                .extracting("baseErrorCode")
                .isEqualTo(MemberErrorCode.DELETED_MEMBER);
        then(cacheRepository).should(never()).set(anyString(), anyString(), any());
    }
}
