package com.kkirok.server.domain.notification.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkirok.server.domain.member.application.service.NotificationAgreeService;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.notification.dao.NotificationRepository;
import com.kkirok.server.domain.notification.domain.Notification;
import com.kkirok.server.domain.notification.domain.NotificationType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryService notificationDeliveryService;
    private final ObjectMapper objectMapper;
    private final NotificationAgreeService notificationAgreeService;

    /**
     * 즉시 트랙: 커밋 직후 바로 FCM 발송까지 이어진다.
     * 지연 자체가 가치 손실인 타입(KKINIPOP_REACTION, MISSION_START)에 사용한다.
     */
    @Transactional
    public void dispatchInstant(
            List<Member> targets,
            NotificationType type,
            String title,
            String body,
            Map<String, String> data
    ) {
        DispatchResult result = saveNotifications(targets, type, title, body, data);
        if (result == null) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // deliver()는 @Async라 이 스레드로 예외가 전파되지 않는다. 실패 로깅은 AsyncConfig의 AsyncUncaughtExceptionHandler가 담당한다.
                notificationDeliveryService.deliver(result.notificationMemberIds(), List.copyOf(result.notificationsById().values()));
            }
        });
    }

    /**
     * 배치 트랙: 지연되거나 여러 건이 묶여 처리돼도 무방한 타입(GROUP_JOIN, MEAL_REMINDER_*)에 사용한다.
     * TODO: NotificationDeliveryScheduler 도입 시 afterCommit에서 즉시 deliver() 호출을 제거하고
     *       저장만 한 뒤 스케줄러가 주기적으로 모아서 발송하도록 변경해야 한다.
     *       그 전까지는 dispatchInstant()와 동일하게 즉시 발송한다.
     */
    @Transactional
    public void dispatchBatched(
            List<Member> targets,
            NotificationType type,
            String title,
            String body,
            Map<String, String> data
    ) {
        dispatchInstant(targets, type, title, body, data);
    }

    private DispatchResult saveNotifications(
            List<Member> targets,
            NotificationType type,
            String title,
            String body,
            Map<String, String> data
    ) {
        if (targets == null || targets.isEmpty()) {
            return null;
        }

        // 데이터 조회
        Set<Long> targetMemberIds = targets.stream().map(Member::getId).collect(Collectors.toSet());
        Set<Long> optedOutMemberIds = notificationAgreeService.findOptedOutMemberIds(targetMemberIds, type.getAgreeType());

        List<Member> filteredTargets = targets.stream()
                .filter(member -> !optedOutMemberIds.contains(member.getId())) // 알림 동의하지 않은 멤버 필터링
                .toList();

        if (filteredTargets.isEmpty()) {
            return null;
        }

        Map<Long, Long> notificationMemberIds = new LinkedHashMap<>();
        Map<Long, Notification> notificationsById = new LinkedHashMap<>();
        Map<String, String> safeData = data == null ? Map.of() : new LinkedHashMap<>(data);
        String dataJson = serialize(safeData);

        for (Member target : filteredTargets) {
            Notification notification = notificationRepository.save(
                    Notification.create(target, type, title, body, null, dataJson) // TODO: 나중에 batch insert 등으로 저장 쿼리 개선해야 할듯
            );
            notificationMemberIds.put(notification.getId(), target.getId());
            notificationsById.put(notification.getId(), notification);
        }

        log.info("Notification dispatch prepared: type={}, targetCount={}, notificationIds={}",
                type, filteredTargets.size(), notificationMemberIds.keySet());

        return new DispatchResult(notificationMemberIds, notificationsById);
    }

    private record DispatchResult(Map<Long, Long> notificationMemberIds, Map<Long, Notification> notificationsById) {
    }

    private String serialize(Map<String, String> data) {
        try {
            return objectMapper.writeValueAsString(data == null ? Map.of() : new LinkedHashMap<>(data));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize notification data", e);
        }
    }
}
