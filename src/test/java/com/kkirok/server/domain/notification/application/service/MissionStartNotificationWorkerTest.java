package com.kkirok.server.domain.notification.application.service;

import com.kkirok.server.domain.kkinipop.application.dto.event.KkinipopMissionStartedEvent;
import com.kkirok.server.domain.kkinipop.application.dto.request.KkinipopGroupCreateRequest;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupMemberRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroup;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import com.kkirok.server.domain.kkinipop.domain.KkinipopMission;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.notification.application.service.NotificationDispatchLogService;
import com.kkirok.server.domain.notification.domain.NotificationType;
import com.kkirok.server.support.fixture.MemberFixture;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MissionStartNotificationWorkerTest {

    @Mock
    private KkinipopGroupMemberRepository groupMemberRepository;

    @Mock
    private NotificationDispatchLogService notificationDispatchLogService;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private MissionStartNotificationWorker missionStartNotificationWorker;

    @Test
    @DisplayName("이미 발송한 미션 시작 알림은 dispatch log 충돌 시 건너뛴다")
    void shouldSkipWhenDispatchLogAlreadyExists() {
        // Given
        KkinipopGroup group = createGroup(10L);
        KkinipopMission mission = createMission(100L, group);

        given(notificationDispatchLogService.claim(NotificationType.MISSION_START, 100L)).willReturn(false);

        // When
        missionStartNotificationWorker.dispatchMission(mission);

        // Then
        then(notificationDispatcher).shouldHaveNoInteractions();
        then(groupMemberRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("새 미션 시작 알림은 활성 멤버에게 발송을 위임한다")
    void shouldDispatchToActiveMembers() {
        // Given
        KkinipopGroup group = createGroup(10L);
        KkinipopMission mission = createMission(100L, group);
        Member first = createMember(1L, "첫번째");
        Member second = createMember(2L, "두번째");

        KkinipopGroupMember firstMembership = KkinipopGroupMember.createMember(group, first);
        KkinipopGroupMember secondMembership = KkinipopGroupMember.createMember(group, second);

        given(notificationDispatchLogService.claim(NotificationType.MISSION_START, 100L)).willReturn(true);
        given(groupMemberRepository.findActiveGroupMembers(10L)).willReturn(List.of(firstMembership, secondMembership));

        // When
        missionStartNotificationWorker.dispatchMission(mission);

        // Then
        then(notificationDispatcher).should().dispatchToMembers(
                List.of(first, second),
                NotificationType.MISSION_START,
                "끼니팝",
                "아침 미션",
                java.util.Map.of(
                        "type", "MISSION_START",
                        "groupId", "10",
                        "missionId", "100"
                )
        );
        then(eventPublisher).should().publishEvent(new KkinipopMissionStartedEvent(10L, 100L));
    }

    private KkinipopGroup createGroup(Long groupId) {
        KkinipopGroup group = KkinipopGroup.create(new KkinipopGroupCreateRequest("아침 챌린저스"), "AB12CD");
        ReflectionTestUtils.setField(group, "id", groupId);
        return group;
    }

    private KkinipopMission createMission(Long missionId, KkinipopGroup group) {
        LocalDateTime now = LocalDateTime.of(2026, 5, 5, 12, 34);
        KkinipopMission mission = KkinipopMission.create(group, "아침 미션", now, now.plusMinutes(10));
        ReflectionTestUtils.setField(mission, "id", missionId);
        return mission;
    }

    private Member createMember(Long memberId, String nickname) {
        Member member = MemberFixture.createLocalMember(nickname, nickname + "@test.com");
        ReflectionTestUtils.setField(member, "id", memberId);
        return member;
    }
}
