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
        // 스케줄러가 5분 주기로 돌기 때문에 조회 구간도 5분으로 맞춰서, 5의 배수 분이 아닌 시각에 시작하는 미션이 어느 구간에도 걸리지 않고 누락되는 걸 방지한다.
        // 겹치는 구간에서 중복 조회돼도 dispatchMission() 내부의 claim()이 mission id 기준으로 중복 발송을 막아준다.
        List<KkinipopMission> missions = missionRepository.findStartingBetween(now.minusMinutes(4), now.plusMinutes(1));

        for (KkinipopMission mission : missions) {
            try {
                missionStartNotificationWorker.dispatchMission(mission);
            } catch (RuntimeException e) {
                log.warn("Failed to dispatch mission-start notification for mission {}", mission.getId(), e);
            }
        }
    }
}
