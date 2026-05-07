package com.kkirok.server.domain.notification.application.service;

import com.kkirok.server.domain.kkinipop.dao.KkinipopMissionRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopMission;
import com.kkirok.server.global.common.util.DateTimeProvider;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MissionStartNotificationService {

    private final KkinipopMissionRepository missionRepository;
    private final MissionStartNotificationWorker missionStartNotificationWorker;
    private final DateTimeProvider dateTimeProvider;

    @Transactional
    public void dispatchStartingMissions() {
        LocalDateTime now = dateTimeProvider.now().truncatedTo(ChronoUnit.MINUTES);
        List<KkinipopMission> missions = missionRepository.findStartingBetween(now, now.plusMinutes(1));

        for (KkinipopMission mission : missions) {
            try {
                missionStartNotificationWorker.dispatchMission(mission);
            } catch (RuntimeException e) {
                log.warn("Failed to dispatch mission-start notification for mission {}", mission.getId(), e);
            }
        }
    }
}
