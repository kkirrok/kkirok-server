package com.kkirok.server.domain.notification.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkirok.server.domain.notification.application.sender.PushPayload;
import com.kkirok.server.domain.notification.application.sender.PushResult;
import com.kkirok.server.domain.notification.application.sender.PushSender;
import com.kkirok.server.domain.notification.dao.DeviceRepository;
import com.kkirok.server.domain.notification.dao.NotificationRepository;
import com.kkirok.server.domain.notification.domain.Device;
import com.kkirok.server.domain.notification.domain.Notification;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.kkirok.server.global.external.r2.application.service.PresignedUrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDeliveryService {

    private final NotificationRepository notificationRepository;
    private final DeviceRepository deviceRepository;
    private final PushSender pushSender;
    private final PresignedUrlService presignedUrlService;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW) // 알림 발송 중 예외 발생 시 상위 트랜잭션에 영향이 가지 않도록 함
    public void deliver(Map<Long, Long> notificationMemberIds, Collection<Notification> notifications) {
        if (notificationMemberIds.isEmpty() || notifications == null || notifications.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        // 발송 대상 Notification을 다시 읽어와 상태 변경을 영속화할 준비를 함.
        Map<Long, Notification> notificationsById = notificationRepository.findAllById(notificationMemberIds.keySet()).stream()
                .collect(Collectors.toMap(Notification::getId, notification -> notification, (left, right) -> left, LinkedHashMap::new));

        // member별 디바이스 토큰을 모아 FCM 멀티캐스트 입력을 만든다.
        Set<Long> memberIds = Set.copyOf(notificationMemberIds.values());
        Map<Long, List<String>> tokensByMemberId = deviceRepository.findAllByMember_IdIn(memberIds).stream()
                .collect(Collectors.groupingBy(
                        device -> device.getMember().getId(),
                        Collectors.mapping(Device::getToken, Collectors.toList())
                ));

        int sentCount = 0;
        int failedCount = 0;

        for (Notification notification : notifications) {
            Notification managedNotification = notificationsById.get(notification.getId());
            if (managedNotification == null) {
                log.warn("Notification not found for delivery, skipping: notificationId={}", notification.getId());
                continue;
            }

            Long memberId = notification.getMember().getId();

            // 푸시 토큰이 없으면 발송은 건너뛰고 실패 상태만 남긴다.
            List<String> tokens = tokensByMemberId.getOrDefault(memberId, List.of());
            if (tokens.isEmpty()) {
                managedNotification.markFailed(now, "NO_DEVICE");
                failedCount++;
                continue;
            }

            // 기타 데이터
            Map<String, String> payloadData = new LinkedHashMap<>(parseData(notification.getDataJson()));
            payloadData.put("notificationId", String.valueOf(notification.getId()));

            // 알림 이미지
            String imageUrl = null;
            if (StringUtils.hasText(notification.getImage())) {
                imageUrl = presignedUrlService.getPresignedUrl(notification.getImage()).toString();
            }

            try {
                // FCM 발송 결과를 기준으로 sentAt / failedAt 과 invalid 토큰 삭제를 반영한다.
                PushResult result = pushSender.sendMulticast(
                        tokens,
                        new PushPayload(notification.getTitle(), notification.getBody(), imageUrl, payloadData)
                );
                if (result.successCount() > 0) {
                    managedNotification.markSent(now);
                    sentCount++;
                } else {
                    managedNotification.markFailed(now, "FCM_NO_SUCCESS");
                    failedCount++;
                }
                deleteInvalidTokens(result.invalidTokens());
            } catch (RuntimeException e) {
                log.warn("Failed to send notification {} to member {}", notification.getId(), memberId, e);
                managedNotification.markFailed(now, e.getMessage());
                failedCount++;
            }
        }

        log.info("Notification delivery finished: total={}, sent={}, failed={}",
                notifications.size(), sentCount, failedCount);
    }

    private Map<String, String> parseData(String dataJson) {
        if (dataJson == null || dataJson.isBlank()) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(dataJson, new TypeReference<LinkedHashMap<String, String>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse notification data", e);
        }
    }

    private void deleteInvalidTokens(Collection<String> invalidTokens) {
        // 앱에서 더 이상 유효하지 않은 토큰만 제거한다.
        if (invalidTokens == null || invalidTokens.isEmpty()) {
            return;
        }
        deviceRepository.deleteByTokenIn(invalidTokens);
    }
}
