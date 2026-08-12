package com.kkirok.server.domain.sse.application.listener;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.kkirok.server.domain.kkinipop.application.dto.event.KkinipopMissionStartedEvent;
import com.kkirok.server.domain.kkinipop.application.dto.request.KkinipopGroupCreateRequest;
import com.kkirok.server.domain.kkinipop.dao.KkinipopMissionRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroup;
import com.kkirok.server.domain.kkinipop.domain.KkinipopMission;
import com.kkirok.server.domain.sse.application.dto.payload.SseMissionStartedPayload;
import com.kkirok.server.domain.sse.application.service.SseEventPublisher;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SseMissionStartedListenerTest {

    @Mock private KkinipopMissionRepository missionRepository;
    @Mock private SseEventPublisher sseEventPublisher;
    @InjectMocks private SseMissionStartedListener listener;

    @Test
    void handle_publishesMissionStartedPayload() {
        KkinipopGroup group = KkinipopGroup.create(new KkinipopGroupCreateRequest("그룹"), "ABC123");
        ReflectionTestUtils.setField(group, "id", 10L);
        LocalDateTime now = LocalDateTime.of(2026, 5, 5, 12, 34);
        KkinipopMission mission = KkinipopMission.create(group, "아침 미션", now, now.plusMinutes(10));
        ReflectionTestUtils.setField(mission, "id", 100L);
        given(missionRepository.findById(100L)).willReturn(Optional.of(mission));

        listener.handle(new KkinipopMissionStartedEvent(10L, 100L));

        ArgumentCaptor<SseMissionStartedPayload> captor = ArgumentCaptor.forClass(SseMissionStartedPayload.class);
        then(sseEventPublisher).should().publish(org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.eq("mission-started"), captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().mission().missionId()).isEqualTo(100L);
    }
}
