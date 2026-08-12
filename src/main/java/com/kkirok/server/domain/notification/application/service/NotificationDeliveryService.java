package com.kkirok.server.domain.notification.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kkirok.server.domain.notification.application.sender.PushBatchItemResult;
import com.kkirok.server.domain.notification.application.sender.PushBatchMessage;
import com.kkirok.server.domain.notification.application.sender.PushPayload;
import com.kkirok.server.domain.notification.application.sender.PushResult;
import com.kkirok.server.domain.notification.application.sender.PushSender;
import com.kkirok.server.domain.notification.dao.DeviceRepository;
import com.kkirok.server.domain.notification.dao.NotificationRepository;
import com.kkirok.server.domain.notification.domain.Device;
import com.kkirok.server.domain.notification.domain.Notification;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.kkirok.server.global.external.r2.application.service.PresignedUrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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

    @Async // 요청/스케줄러 스레드가 FCM 네트워크 호출 때문에 지연되지 않도록 별도 스레드에서 실행
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

    /**
     * 배치 트랙(GROUP_JOIN, MEAL_REMINDER_*)의 미발송/재시도 대상 알림을 모아서 sendEach()로 배치 발송한다.
     * 넘겨받은 notifications는 스케줄러 자신의 트랜잭션에서 조회된 detached 엔티티이므로,
     * 이 메서드의 REQUIRES_NEW 트랜잭션 안에서 findAllById()로 다시 조회해 managed 상태로 만든 뒤 수정한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deliverPendingBatch(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        List<Long> notificationIds = notifications.stream().map(Notification::getId).toList();
        Map<Long, Notification> managedNotificationsById = notificationRepository.findAllById(notificationIds).stream()
                .collect(Collectors.toMap(Notification::getId, notification -> notification, (left, right) -> left, LinkedHashMap::new));

        Set<Long> memberIds = notifications.stream()
                .map(notification -> notification.getMember().getId())
                .collect(Collectors.toSet());
        Map<Long, List<String>> tokensByMemberId = deviceRepository.findAllByMember_IdIn(memberIds).stream()
                .collect(Collectors.groupingBy(
                        device -> device.getMember().getId(),
                        Collectors.mapping(Device::getToken, Collectors.toList())
                ));

        List<PushBatchMessage> messages = new ArrayList<>();
        List<Notification> messageOwners = new ArrayList<>();
        int noDeviceCount = 0;

        for (Notification notification : notifications) {
            Notification managedNotification = managedNotificationsById.get(notification.getId());
            if (managedNotification == null) {
                log.warn("Notification not found for pending delivery, skipping: notificationId={}", notification.getId());
                continue;
            }

            List<String> tokens = tokensByMemberId.getOrDefault(notification.getMember().getId(), List.of());
            if (tokens.isEmpty()) {
                managedNotification.markFailed(now, "NO_DEVICE");
                noDeviceCount++;
                continue;
            }

            Map<String, String> payloadData = new LinkedHashMap<>(parseData(notification.getDataJson()));
            payloadData.put("notificationId", String.valueOf(notification.getId()));

            String imageUrl = null;
            if (StringUtils.hasText(notification.getImage())) {
                imageUrl = presignedUrlService.getPresignedUrl(notification.getImage()).toString();
            }

            PushPayload payload = new PushPayload(notification.getTitle(), notification.getBody(), imageUrl, payloadData);
            for (String token : tokens) {
                messages.add(new PushBatchMessage(token, payload));
                messageOwners.add(managedNotification);
            }
        }

        int sentCount = 0;
        int failedCount = 0;

        if (!messages.isEmpty()) {
            List<PushBatchItemResult> results;
            try {
                results = pushSender.sendEach(messages);
            } catch (RuntimeException e) {
                // sendEach() 예외가 그대로 전파되면 REQUIRES_NEW 트랜잭션 전체가 롤백되어
                // 이미 처리한 NO_DEVICE 마킹까지 유실되므로, 여기서 잡아 배치 단위 실패로 격리한다.
                log.warn("Failed to send pending notification batch: batchSize={}", messages.size(), e);
                for (Notification owner : new LinkedHashSet<>(messageOwners)) {
                    owner.markFailed(now, "FCM_BATCH_SEND_FAILED");
                    failedCount++;
                }
                results = null;
            }

            if (results != null) {
                Map<Notification, Boolean> successByNotification = new LinkedHashMap<>();
                List<String> invalidTokens = new ArrayList<>();
                for (int index = 0; index < results.size(); index++) {
                    PushBatchItemResult result = results.get(index);
                    Notification owner = messageOwners.get(index);
                    successByNotification.merge(owner, result.success(), (existing, current) -> existing || current);
                    if (result.invalidToken()) {
                        invalidTokens.add(result.token());
                    }
                }

                for (Map.Entry<Notification, Boolean> entry : successByNotification.entrySet()) {
                    if (entry.getValue()) {
                        entry.getKey().markSent(now);
                        sentCount++;
                    } else {
                        entry.getKey().markFailed(now, "FCM_NO_SUCCESS");
                        failedCount++;
                    }
                }
                deleteInvalidTokens(invalidTokens);
            }
        }

        log.info("Pending notification batch finished: total={}, sent={}, failed={}",
                notifications.size(), sentCount, failedCount + noDeviceCount);
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
