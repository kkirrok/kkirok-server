package com.kkirok.server.domain.member.api;

import com.kkirok.server.domain.member.application.service.AccountRecoveryService;
import com.kkirok.server.domain.member.application.service.MemberService;
import com.kkirok.server.domain.member.application.service.MyPageService;
import com.kkirok.server.domain.member.application.service.NotificationAgreeService;
import com.kkirok.server.domain.member.application.service.OnboardingService;
import com.kkirok.server.global.auth.jwt.application.ActiveCheckCacheService;
import com.kkirok.server.global.auth.jwt.application.RoleCacheService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    private AccountRecoveryService accountRecoveryService;

    @Mock
    private OnboardingService onboardingService;

    @Mock
    private MemberService memberService;

    @Mock
    private MyPageService myPageService;

    @Mock
    private NotificationAgreeService notificationAgreeService;

    @Mock
    private RoleCacheService roleCacheService;

    @Mock
    private ActiveCheckCacheService activeCheckCacheService;

    @InjectMocks
    private MemberController memberController;

    @Test
    @DisplayName("회원 탈퇴 시 memberService.quit 호출 이후 role 캐시를 무효화한다")
    void shouldEvictRoleCache_afterQuit() {
        // Given
        Long memberId = 1L;

        // When
        memberController.quitMember(memberId);

        // Then
        InOrder inOrder = inOrder(memberService, roleCacheService, activeCheckCacheService);
        inOrder.verify(memberService).quit(memberId);
        inOrder.verify(roleCacheService).evictRole(memberId);
        inOrder.verify(activeCheckCacheService).evictActive(memberId);
    }
}
