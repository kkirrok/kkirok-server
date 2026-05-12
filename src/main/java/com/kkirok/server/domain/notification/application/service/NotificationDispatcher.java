package com.kkirok.server.domain.notification.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.notification.dao.NotificationRepository;
import com.kkirok.server.domain.notification.domain.Notification;
import com.kkirok.server.domain.notification.domain.NotificationType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

        // TODO: 유저의 알림 허용 정책 적용
        Map<Long, Long> notificationMemberIds = new LinkedHashMap<>();
        Map<String, String> safeData = data == null ? Map.of() : new LinkedHashMap<>(data);
        String dataJson = serialize(safeData);

        for (Member target : targets) {
            Notification notification = notificationRepository.save(
                    Notification.create(target, type, title, body, dataJson) // 나중에 batch insert 등으로 저장 쿼리 개선해야 할듯
            );
            notificationMemberIds.put(notification.getId(), target.getId());
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                notificationDeliveryService.deliver(notificationMemberIds, title, body, safeData);
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
