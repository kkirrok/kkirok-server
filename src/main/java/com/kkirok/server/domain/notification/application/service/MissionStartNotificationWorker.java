package com.kkirok.server.domain.notification.application.service;

import com.kkirok.server.domain.kkinipop.application.dto.event.KkinipopMissionStartedEvent;
import com.kkirok.server.domain.kkinipop.dao.KkinipopGroupMemberRepository;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupMember;
import com.kkirok.server.domain.kkinipop.domain.KkinipopMission;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.notification.domain.NotificationType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 실시간 미션이 있으면 알림을 발송합니다. -> NotificationDispatcher.dispatchInstant() 호출 (즉시 트랙)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MissionStartNotificationWorker {

    private final KkinipopGroupMemberRepository groupMemberRepository;
    private final NotificationDispatchLogService notificationDispatchLogService;
    private final NotificationDispatcher notificationDispatcher;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW) // 알림 발송 중 예외 발생 시 상위 트랜잭션에 영향이 가지 않도록 함
    public void dispatchMission(KkinipopMission mission) {

        if (!notificationDispatchLogService.claim(NotificationType.MISSION_START, mission.getId())) {
            return;
        }

        List<Member> recipients = groupMemberRepository.findActiveGroupMembers(mission.getGroup().getId()).stream()
                .map(KkinipopGroupMember::getMember)
                .toList();

        if (recipients.isEmpty()) {
            return;
        }

        Map<String, String> data = new LinkedHashMap<>();
        data.put("type", NotificationType.MISSION_START.name());
        data.put("groupId", String.valueOf(mission.getGroup().getId()));
        data.put("missionId", String.valueOf(mission.getId()));

        notificationDispatcher.dispatchInstant(
                recipients,
                NotificationType.MISSION_START,
                "끼니팝",
                mission.getTitle(),
                data
        );

        // SSE는 다른 리스너들과 동일하게 트랜잭션 커밋 후에만 쏘도록 이벤트로 위임한다.
        eventPublisher.publishEvent(new KkinipopMissionStartedEvent(mission.getGroup().getId(), mission.getId()));
    }
}
