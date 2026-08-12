package com.kkirok.server.domain.sse.application.listener;

import com.kkirok.server.domain.kkinipop.application.dto.event.KkinipopMissionStartedEvent;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopMissionResponse;
import com.kkirok.server.domain.kkinipop.dao.KkinipopMissionRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopMission;
import com.kkirok.server.domain.sse.application.dto.payload.SseMissionStartedPayload;
import com.kkirok.server.domain.sse.application.service.SseEventPublisher;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SseMissionStartedListener {

    private final KkinipopMissionRepository missionRepository;
    private final SseEventPublisher sseEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(KkinipopMissionStartedEvent event) {
        KkinipopMission mission = missionRepository.findById(event.missionId()).orElse(null);
        if (mission == null) {
            return;
        }

        KkinipopMissionResponse missionResponse = KkinipopMissionResponse.from(mission, LocalDateTime.now(), List.of());
        sseEventPublisher.publish(event.groupId(), "mission-started",
                new SseMissionStartedPayload(event.groupId(), missionResponse));
    }
}
