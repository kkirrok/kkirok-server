package com.kkirok.server.domain.notification.exception;

import com.kkirok.server.global.common.exception.base.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationSuccessCode implements BaseSuccessCode {
    DEVICE_REGISTER_SUCCESS(200, "디바이스 등록 성공"),
    DEVICE_UNREGISTER_SUCCESS(200, "디바이스 해제 성공"),
    NOTIFICATION_LIST_GET_SUCCESS(200, "알림함 조회 성공"),
    UNREAD_COUNT_GET_SUCCESS(200, "읽지 않은 알림 수 조회 성공"),
    NOTIFICATION_READ_SUCCESS(200, "알림 읽음 처리 성공"),
    NOTIFICATION_READ_ALL_SUCCESS(200, "모든 알림 읽음 처리 성공"),
    ;

    private final int status;
    private final String message;
}
