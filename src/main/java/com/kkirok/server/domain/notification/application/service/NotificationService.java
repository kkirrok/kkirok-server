package com.kkirok.server.domain.notification.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.notification.application.dto.request.DeviceRegisterRequest;
import com.kkirok.server.domain.notification.application.dto.request.DeviceUnregisterRequest;
import com.kkirok.server.domain.notification.application.dto.response.NotificationPageResponse;
import com.kkirok.server.domain.notification.application.dto.response.NotificationResponse;
import com.kkirok.server.domain.notification.application.dto.response.UnreadCountResponse;
import com.kkirok.server.domain.notification.dao.DeviceRepository;
import com.kkirok.server.domain.notification.dao.NotificationRepository;
import com.kkirok.server.domain.notification.domain.Device;
import com.kkirok.server.domain.notification.domain.Notification;
import com.kkirok.server.domain.notification.exception.NotificationErrorCode;
import com.kkirok.server.global.common.exception.NotFoundException;
import com.kkirok.server.global.common.util.DateTimeProvider;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final MemberUseCase memberUseCase;
    private final NotificationRepository notificationRepository;
    private final DeviceRepository deviceRepository;
    private final DateTimeProvider dateTimeProvider;
    private final ObjectMapper objectMapper;

    @Transactional
    public void registerDevice(Long memberId, DeviceRegisterRequest request) {
        Member member = memberUseCase.findMemberByMemberId(memberId);
        LocalDateTime now = dateTimeProvider.now();

        Device device = deviceRepository.findByToken(request.token())
                .orElseGet(() -> Device.create(member, request.token(), request.platform(), now));
        device.touch(member, request.platform(), now);
        deviceRepository.save(device);
    }

    @Transactional
    public void unregisterDevice(Long memberId, DeviceUnregisterRequest request) {
        Device device = deviceRepository.findByToken(request.token())
                .orElseThrow(() -> new NotFoundException(NotificationErrorCode.DEVICE_NOT_FOUND));

        if (!device.getMember().getId().equals(memberId)) {
            throw new NotFoundException(NotificationErrorCode.DEVICE_NOT_FOUND);
        }

        deviceRepository.delete(device);
    }

    public NotificationPageResponse getNotifications(Long memberId, int page, int size) {

        Page<Notification> notifications = notificationRepository.findAllByMember_IdOrderByCreatedAtDesc(
                memberId,
                PageRequest.of(page, size)
        );

        List<NotificationResponse> content = notifications.stream()
                .map(this::toResponse)
                .toList();

        return NotificationPageResponse.from(notifications, content);
    }

    public UnreadCountResponse getUnreadCount(Long memberId) {
        return new UnreadCountResponse(notificationRepository.countByMember_IdAndReadAtIsNull(memberId));
    }

    @Transactional
    public void readNotification(Long memberId, Long notificationId) {
        int updated = notificationRepository.markRead(notificationId, memberId, dateTimeProvider.now());
        if (updated == 0) {
            throw new NotFoundException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
        }
    }

    @Transactional
    public void readAll(Long memberId) {
        notificationRepository.markAllRead(memberId, dateTimeProvider.now());
    }



    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.from(notification, parseData(notification.getDataJson()));
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
}
