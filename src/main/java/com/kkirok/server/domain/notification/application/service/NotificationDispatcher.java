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

    @Transactional
    public void dispatchToMembers(
            List<Member> targets,
            NotificationType type,
            String title,
            String body,
            Map<String, String> data
    ) {
        if (targets == null || targets.isEmpty()) {
            return;
        }

        // 데이터 조회
        Set<Long> targetMemberIds = targets.stream().map(Member::getId).collect(Collectors.toSet());
        Set<Long> optedOutMemberIds = notificationAgreeService.findOptedOutMemberIds(targetMemberIds, type.getAgreeType());

        List<Member> filteredTargets = targets.stream()
                .filter(member -> !optedOutMemberIds.contains(member.getId())) // 알림 동의하지 않은 멤버 필터링
                .toList();

        if (filteredTargets.isEmpty()) {
            return;
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

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // deliver()는 @Async라 이 스레드로 예외가 전파되지 않는다. 실패 로깅은 AsyncConfig의 AsyncUncaughtExceptionHandler가 담당한다.
                notificationDeliveryService.deliver(notificationMemberIds, List.copyOf(notificationsById.values()));
            }
        });
    }

    private String serialize(Map<String, String> data) {
        try {
            return objectMapper.writeValueAsString(data == null ? Map.of() : new LinkedHashMap<>(data));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize notification data", e);
        }
    }
}
