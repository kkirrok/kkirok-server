package com.kkirok.server.domain.notification.application.service;

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
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationDeliveryService {

    private final NotificationRepository notificationRepository;
    private final DeviceRepository deviceRepository;
    private final PushSender pushSender;

    @Transactional(propagation = Propagation.REQUIRES_NEW) // 알림 발송 중 예외 발생 시 상위 트랜잭션에 영향이 가지 않도록 함
    public void deliver(Map<Long, Long> notificationMemberIds, String title, String body, Map<String, String> data) {
        if (notificationMemberIds.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        // 발송 대상 Notification을 다시 읽어와 상태 변경을 영속화할 준비를 함.
        Map<Long, Notification> notificationsById = StreamSupport.stream(notificationRepository.findAllById(notificationMemberIds.keySet()).spliterator(), false)
                .collect(Collectors.toMap(Notification::getId, notification -> notification, (left, right) -> left, LinkedHashMap::new));

        // member별 디바이스 토큰을 모아 FCM 멀티캐스트 입력을 만든다.
        Set<Long> memberIds = Set.copyOf(notificationMemberIds.values());
        Map<Long, List<String>> tokensByMemberId = deviceRepository.findAllByMember_IdIn(memberIds).stream()
                .collect(Collectors.groupingBy(
                        device -> device.getMember().getId(),
                        Collectors.mapping(Device::getToken, Collectors.toList())
                ));

        for (Map.Entry<Long, Long> entry : notificationMemberIds.entrySet()) {
            Long notificationId = entry.getKey();
            Long memberId = entry.getValue();
            Notification notification = notificationsById.get(notificationId);
            if (notification == null) {
                continue;
            }

            // 푸시 토큰이 없으면 발송은 건너뛰고 실패 상태만 남긴다.
            List<String> tokens = tokensByMemberId.getOrDefault(memberId, List.of());
            if (tokens.isEmpty()) {
                notification.markFailed(now, "NO_DEVICE");
                continue;
            }

            // 클라이언트 라우팅용 notificationId를 payload에 포함한다.
            Map<String, String> payloadData = new LinkedHashMap<>(data);
            payloadData.put("notificationId", String.valueOf(notificationId));

            try {
                // FCM 발송 결과를 기준으로 sentAt / failedAt 과 invalid 토큰 삭제를 반영한다.
                PushResult result = pushSender.sendMulticast(tokens, new PushPayload(title, body, payloadData));
                if (result.successCount() > 0) {
                    notification.markSent(now);
                } else {
                    notification.markFailed(now, "FCM_NO_SUCCESS");
                }
                deleteInvalidTokens(result.invalidTokens());
            } catch (RuntimeException e) {
                log.warn("Failed to send notification {} to member {}", notificationId, memberId, e);
                notification.markFailed(now, e.getMessage());
            }
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
