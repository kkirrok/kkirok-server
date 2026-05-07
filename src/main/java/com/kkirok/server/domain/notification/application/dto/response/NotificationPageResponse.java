package com.kkirok.server.domain.notification.application.dto.response;

import com.kkirok.server.domain.notification.domain.Notification;
import com.kkirok.server.global.common.dto.PageInfoResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(description = "알림함 페이지 응답")
public record NotificationPageResponse(
        @Schema(description = "알림 목록", implementation = NotificationResponse.class)
        List<NotificationResponse> notifications,
        @Schema(description = "페이지 정보", implementation = PageInfoResponse.class)
        PageInfoResponse pageInfo
) {
    public static NotificationPageResponse from(Page<Notification> page, List<NotificationResponse> notifications) {
        return new NotificationPageResponse(
                notifications,
                PageInfoResponse.from(page)
        );
    }
}
