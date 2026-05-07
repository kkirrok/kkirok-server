package com.kkirok.server.domain.notification.api;

import com.kkirok.server.domain.notification.application.dto.request.DeviceRegisterRequest;
import com.kkirok.server.domain.notification.application.dto.request.DeviceUnregisterRequest;
import com.kkirok.server.domain.notification.application.dto.response.NotificationPageResponse;
import com.kkirok.server.domain.notification.application.dto.response.UnreadCountResponse;
import com.kkirok.server.domain.notification.application.service.NotificationService;
import com.kkirok.server.domain.notification.exception.NotificationSuccessCode;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.auth.annotation.RoleUserAuth;
import com.kkirok.server.global.common.dto.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@RoleUserAuth
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    @Override
    @PostMapping("/devices")
    public ResponseEntity<SuccessResponse<Void>> registerDevice(
            @CurrentMember Long memberId,
            @Valid @RequestBody DeviceRegisterRequest request
    ) {
        notificationService.registerDevice(memberId, request);
        return ResponseEntity.ok(SuccessResponse.of(NotificationSuccessCode.DEVICE_REGISTER_SUCCESS, null));
    }

    @Override
    @DeleteMapping("/devices")
    public ResponseEntity<SuccessResponse<Void>> unregisterDevice(
            @CurrentMember Long memberId,
            @Valid @RequestBody DeviceUnregisterRequest request
    ) {
        notificationService.unregisterDevice(memberId, request);
        return ResponseEntity.ok(SuccessResponse.of(NotificationSuccessCode.DEVICE_UNREGISTER_SUCCESS, null));
    }

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse<NotificationPageResponse>> getNotifications(
            @CurrentMember Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(SuccessResponse.of(
                NotificationSuccessCode.NOTIFICATION_LIST_GET_SUCCESS,
                notificationService.getNotifications(memberId, page, size)
        ));
    }

    @Override
    @GetMapping("/unread-count")
    public ResponseEntity<SuccessResponse<UnreadCountResponse>> getUnreadCount(@CurrentMember Long memberId) {
        return ResponseEntity.ok(SuccessResponse.of(
                NotificationSuccessCode.UNREAD_COUNT_GET_SUCCESS,
                notificationService.getUnreadCount(memberId)
        ));
    }

    @Override
    @PatchMapping("/{id}/read")
    public ResponseEntity<SuccessResponse<Void>> readNotification(
            @CurrentMember Long memberId,
            @PathVariable("id") Long notificationId
    ) {
        notificationService.readNotification(memberId, notificationId);
        return ResponseEntity.ok(SuccessResponse.of(NotificationSuccessCode.NOTIFICATION_READ_SUCCESS, null));
    }

    @Override
    @PatchMapping("/read-all")
    public ResponseEntity<SuccessResponse<Void>> readAll(@CurrentMember Long memberId) {
        notificationService.readAll(memberId);
        return ResponseEntity.ok(SuccessResponse.of(NotificationSuccessCode.NOTIFICATION_READ_ALL_SUCCESS, null));
    }
}
