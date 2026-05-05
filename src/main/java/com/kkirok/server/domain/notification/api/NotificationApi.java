package com.kkirok.server.domain.notification.api;

import com.kkirok.server.domain.notification.application.dto.request.DeviceRegisterRequest;
import com.kkirok.server.domain.notification.application.dto.request.DeviceUnregisterRequest;
import com.kkirok.server.domain.notification.application.dto.response.NotificationPageResponse;
import com.kkirok.server.domain.notification.application.dto.response.UnreadCountResponse;
import com.kkirok.server.domain.notification.exception.NotificationErrorCode;
import com.kkirok.server.domain.notification.exception.NotificationSuccessCode;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.common.dto.SuccessResponse;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExample;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExamples;
import com.kkirok.server.global.swagger.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Notification API", description = "알림 관련 API")
public interface NotificationApi {

    @Operation(
            summary = "디바이스 토큰 등록 [USER]",
            description = "로그인한 회원의 FCM 디바이스 토큰을 등록합니다. 같은 토큰이 이미 있으면 새로 만들지 않고 lastUsedAt과 platform을 갱신합니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(status = 400, message = "요청이 유효하지 않습니다.", exampleName = "BAD_REQUEST")
    })
    @ApiSuccessCodeExample(codeType = NotificationSuccessCode.class, code = "DEVICE_REGISTER_SUCCESS")
    ResponseEntity<SuccessResponse<Void>> registerDevice(
            @CurrentMember Long memberId,
            @RequestBody DeviceRegisterRequest request
    );

    @Operation(
            summary = "디바이스 토큰 해제 [USER]",
            description = "로그아웃 시 해당 토큰을 디바이스 목록에서 제거합니다. 토큰이 현재 로그인한 회원의 것이 아니면 삭제하지 않습니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(status = 400, message = "요청이 유효하지 않습니다.", exampleName = "BAD_REQUEST"),
            @ApiErrorCodeExample(codeType = NotificationErrorCode.class, code = "DEVICE_NOT_FOUND")
    })
    @ApiSuccessCodeExample(codeType = NotificationSuccessCode.class, code = "DEVICE_UNREGISTER_SUCCESS")
    ResponseEntity<SuccessResponse<Void>> unregisterDevice(
            @CurrentMember Long memberId,
            @RequestBody DeviceUnregisterRequest request
    );

    @Operation(
            summary = "알림함 조회 [USER]",
            description = "현재 로그인한 회원의 인앱 알림함을 최신순으로 조회합니다. 각 항목은 제목, 본문, 추가 데이터, 읽음 여부, 발송/실패 시각을 포함합니다."
    )
    @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED")
    @ApiSuccessCodeExample(codeType = NotificationSuccessCode.class, code = "NOTIFICATION_LIST_GET_SUCCESS")
    ResponseEntity<SuccessResponse<NotificationPageResponse>> getNotifications(
            @CurrentMember Long memberId,
            @Parameter(description = "페이지 번호", example = "0")
            int page,
            @Parameter(description = "페이지 크기", example = "20")
            int size
    );

    @Operation(
            summary = "읽지 않은 알림 수 조회 [USER]",
            description = "현재 로그인한 회원의 읽지 않은 알림 수를 반환합니다."
    )
    @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED")
    @ApiSuccessCodeExample(codeType = NotificationSuccessCode.class, code = "UNREAD_COUNT_GET_SUCCESS")
    ResponseEntity<SuccessResponse<UnreadCountResponse>> getUnreadCount(
            @Parameter(description = "현재 로그인한 회원 ID", hidden = true)
            @CurrentMember Long memberId
    );

    @Operation(
            summary = "알림 단건 읽음 [USER]",
            description = "지정한 알림을 읽음 처리합니다."
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = NotificationErrorCode.class, code = "NOTIFICATION_NOT_FOUND")
    })
    @ApiSuccessCodeExample(codeType = NotificationSuccessCode.class, code = "NOTIFICATION_READ_SUCCESS")
    ResponseEntity<SuccessResponse<Void>> readNotification(
            @CurrentMember Long memberId,
            @Parameter(description = "알림 ID", required = true)
            @PathVariable("id") Long notificationId
    );

    @Operation(
            summary = "전체 읽음 처리 [USER]",
            description = "현재 로그인한 회원의 읽지 않은 알림을 모두 읽음 처리합니다."
    )
    @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED")
    @ApiSuccessCodeExample(codeType = NotificationSuccessCode.class, code = "NOTIFICATION_READ_ALL_SUCCESS")
    ResponseEntity<SuccessResponse<Void>> readAll(
            @CurrentMember Long memberId
    );
}
