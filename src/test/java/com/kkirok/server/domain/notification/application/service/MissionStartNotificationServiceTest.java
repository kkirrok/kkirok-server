package com.kkirok.server.domain.notification.application.service;

import com.kkirok.server.domain.kkinipop.dao.KkinipopMissionRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroup;
import com.kkirok.server.domain.kkinipop.domain.KkinipopMission;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.global.common.util.DateTimeProvider;
import com.kkirok.server.support.fixture.MemberFixture;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class MissionStartNotificationServiceTest {

    @Mock
    private KkinipopMissionRepository missionRepository;

    @Mock
    private MissionStartNotificationWorker missionStartNotificationWorker;

    @Mock
    private DateTimeProvider dateTimeProvider;

    @InjectMocks
    private MissionStartNotificationService missionStartNotificationService;

    @Test
    @DisplayName("시작 예정 분의 미션을 조회해 각 미션 처리기에 위임한다")
    void shouldDelegateEachStartingMission() {
        // Given
        LocalDateTime now = LocalDateTime.of(2026, 5, 5, 12, 34, 45);
        LocalDateTime windowStart = now.withSecond(0).withNano(0);
        KkinipopGroup group = createGroup(10L);
        KkinipopMission first = createMission(100L, group, "아침 미션", windowStart.plusSeconds(5));
        KkinipopMission second = createMission(101L, group, "점심 미션", windowStart.plusSeconds(40));

        given(dateTimeProvider.now()).willReturn(now);
        given(missionRepository.findStartingBetween(windowStart.minusMinutes(4), windowStart.plusMinutes(1)))
                .willReturn(List.of(first, second));

        // When
        missionStartNotificationService.dispatchStartingMissions();

        // Then
        then(missionStartNotificationWorker).should().dispatchMission(first);
        then(missionStartNotificationWorker).should().dispatchMission(second);
    }

    private KkinipopGroup createGroup(Long groupId) {
        KkinipopGroup group = KkinipopGroup.create(
                new com.kkirok.server.domain.kkinipop.application.dto.request.KkinipopGroupCreateRequest("아침 챌린저스"),
                "AB12CD"
        );
        ReflectionTestUtils.setField(group, "id", groupId);
        return group;
    }

    private KkinipopMission createMission(Long missionId, KkinipopGroup group, String title, LocalDateTime now) {
        KkinipopMission mission = KkinipopMission.create(group, title, now, now.plusMinutes(10));
        ReflectionTestUtils.setField(mission, "id", missionId);
        return mission;
    }
}
