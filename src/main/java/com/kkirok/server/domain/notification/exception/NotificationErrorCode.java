package com.kkirok.server.domain.notification.exception;

import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements BaseErrorCode {
    DEVICE_NOT_FOUND(404, "등록된 디바이스를 찾을 수 없습니다."),
    NOTIFICATION_NOT_FOUND(404, "알림을 찾을 수 없습니다."),
    FCM_INIT_FAILED(500, "FCM 초기화에 실패했습니다."),
    ;

    private final int status;
    private final String message;
}
