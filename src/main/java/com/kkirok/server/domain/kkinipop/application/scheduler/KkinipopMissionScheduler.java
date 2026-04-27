package com.kkirok.server.domain.kkinipop.application.scheduler;

import com.kkirok.server.domain.kkinipop.application.service.KkinipopMissionGenerateService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KkinipopMissionScheduler {

    private final KkinipopMissionGenerateService kkinipopMissionGenerateService;

    /**
     * 매일 23:55에 다음날 미션을 미리 생성한다.
     *
     * <p>예시
     * - 2026-04-24 23:55 실행
     * - 2026-04-25 미션 후보 20개 생성
     * - 각 팀에 5개씩 랜덤 배정
     */
    @Scheduled(cron = "0 55 23 * * *", zone = "Asia/Seoul")
    public void generateNextDayMissions() {
        kkinipopMissionGenerateService.generateNextDayMissions();
    }
}
