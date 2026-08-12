package com.kkirok.server.domain.notification.application.service;

import com.kkirok.server.domain.member.dao.MemberRepository;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.notification.application.policy.MealReminderPolicy;
import com.kkirok.server.domain.notification.application.policy.MealReminderTarget;
import com.kkirok.server.domain.notification.domain.NotificationType;
import com.kkirok.server.global.common.util.DateTimeProvider;
import com.kkirok.server.support.fixture.MemberFixture;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class MealReminderServiceTest {

    @Mock
    private MealReminderPolicy overduePolicy;

    @Mock
    private MealReminderPolicy noMealTodayPolicy;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NotificationDispatchLogService notificationDispatchLogService;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private DateTimeProvider dateTimeProvider;

    @Test
    @DisplayName("활동시간대가 아니면 아무 작업도 하지 않는다")
    void shouldReturnWhenOutsideActiveHours() {
        MealReminderService mealReminderService = new MealReminderService(
                List.of(overduePolicy, noMealTodayPolicy),
                memberRepository,
                notificationDispatchLogService,
                notificationDispatcher,
                dateTimeProvider
        );
        given(dateTimeProvider.now()).willReturn(LocalDateTime.of(2026, 5, 6, 8, 59));

        mealReminderService.runReminderCycle();

        then(overduePolicy).shouldHaveNoInteractions();
        then(noMealTodayPolicy).shouldHaveNoInteractions();
        then(memberRepository).shouldHaveNoInteractions();
        then(notificationDispatchLogService).shouldHaveNoInteractions();
        then(notificationDispatcher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("같은 사이클에서 중복 멤버는 첫 정책만 발송한다")
    void shouldDeduplicateMembersAcrossPolicies() {
        MealReminderService mealReminderService = new MealReminderService(
                List.of(overduePolicy, noMealTodayPolicy),
                memberRepository,
                notificationDispatchLogService,
                notificationDispatcher,
                dateTimeProvider
        );
        LocalDateTime now = LocalDateTime.of(2026, 5, 6, 10, 0);
        Member member1 = createMember(1L, "회원1");
        Member member2 = createMember(2L, "회원2");
        Member member3 = createMember(3L, "회원3");

        given(dateTimeProvider.now()).willReturn(now);
        given(overduePolicy.type()).willReturn(NotificationType.MEAL_REMINDER_OVERDUE);
        given(overduePolicy.title()).willReturn("overdue-title");
        given(overduePolicy.body()).willReturn("overdue-body");
        given(overduePolicy.findTargets(now)).willReturn(List.of(
                new MealReminderTarget(1L, "100"),
                new MealReminderTarget(2L, "101")
        ));
        given(noMealTodayPolicy.type()).willReturn(NotificationType.MEAL_REMINDER_NO_TODAY);
        given(noMealTodayPolicy.title()).willReturn("today-title");
        given(noMealTodayPolicy.body()).willReturn("today-body");
        given(noMealTodayPolicy.findTargets(now)).willReturn(List.of(
                new MealReminderTarget(1L, "DAY-20260506"),
                new MealReminderTarget(3L, "DAY-20260506")
        ));
        given(memberRepository.findAllById(any())).willAnswer(invocation -> {
            Iterable<Long> ids = invocation.getArgument(0);
            List<Long> idList = java.util.stream.StreamSupport.stream(ids.spliterator(), false).toList();
            return idList.stream()
                    .map(id -> switch (id.intValue()) {
                        case 1 -> member1;
                        case 2 -> member2;
                        case 3 -> member3;
                        default -> null;
                    })
                    .filter(Objects::nonNull)
                    .toList();
        });
        given(notificationDispatchLogService.claim(NotificationType.MEAL_REMINDER_OVERDUE, "100:2026-05-06T10:00")).willReturn(true);
        given(notificationDispatchLogService.claim(NotificationType.MEAL_REMINDER_OVERDUE, "101:2026-05-06T10:00")).willReturn(true);
        given(notificationDispatchLogService.claim(NotificationType.MEAL_REMINDER_NO_TODAY, "DAY-20260506:3")).willReturn(true);

        mealReminderService.runReminderCycle();

        then(notificationDispatcher).should(times(3)).dispatchBatched(any(), any(), any(), any(), any());
        then(notificationDispatcher).should().dispatchBatched(
                List.of(member1),
                NotificationType.MEAL_REMINDER_OVERDUE,
                "overdue-title",
                "overdue-body",
                Map.of("type", "MEAL_REMINDER_OVERDUE", "lastMealId", "100")
        );
        then(notificationDispatcher).should().dispatchBatched(
                List.of(member2),
                NotificationType.MEAL_REMINDER_OVERDUE,
                "overdue-title",
                "overdue-body",
                Map.of("type", "MEAL_REMINDER_OVERDUE", "lastMealId", "101")
        );
        then(notificationDispatcher).should().dispatchBatched(
                List.of(member3),
                NotificationType.MEAL_REMINDER_NO_TODAY,
                "today-title",
                "today-body",
                Map.of("type", "MEAL_REMINDER_NO_TODAY", "date", "20260506")
        );
        then(notificationDispatchLogService).should(never())
                .claim(NotificationType.MEAL_REMINDER_NO_TODAY, "DAY-20260506:1");
    }

    @Test
    @DisplayName("claim 충돌이면 해당 멤버는 발송하지 않는다")
    void shouldSkipWhenClaimFails() {
        MealReminderService mealReminderService = new MealReminderService(
                List.of(overduePolicy),
                memberRepository,
                notificationDispatchLogService,
                notificationDispatcher,
                dateTimeProvider
        );
        LocalDateTime now = LocalDateTime.of(2026, 5, 6, 11, 0);
        Member member1 = createMember(1L, "회원1");

        given(dateTimeProvider.now()).willReturn(now);
        given(overduePolicy.type()).willReturn(NotificationType.MEAL_REMINDER_OVERDUE);
        given(overduePolicy.findTargets(now)).willReturn(List.of(new MealReminderTarget(1L, "100")));
        given(memberRepository.findAllById(any())).willReturn(List.of(member1));
        given(notificationDispatchLogService.claim(NotificationType.MEAL_REMINDER_OVERDUE, "100:2026-05-06T11:00")).willReturn(false);

        mealReminderService.runReminderCycle();

        then(notificationDispatcher).should(never()).dispatchBatched(any(), any(), any(), any(), any());
    }

    private Member createMember(Long memberId, String nickname) {
        Member member = MemberFixture.createLocalMember(nickname, nickname + "@test.com");
        ReflectionTestUtils.setField(member, "id", memberId);
        ReflectionTestUtils.setField(member, "onboardingCompleted", true);
        return member;
    }
}
