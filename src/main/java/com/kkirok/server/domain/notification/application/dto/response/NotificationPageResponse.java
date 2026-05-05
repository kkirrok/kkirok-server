package com.kkirok.server.domain.notification.application.dto.response;

import com.kkirok.server.domain.notification.domain.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

@Schema(description = "알림함 페이지 응답")
public record NotificationPageResponse(
        @Schema(description = "알림 목록", implementation = NotificationResponse.class)
        List<NotificationResponse> notifications,
        @Schema(description = "현재 페이지", example = "0")
        int page,
        @Schema(description = "페이지 크기", example = "20")
        int size,
        @Schema(description = "전체 개수", example = "42")
        long totalElements,
        @Schema(description = "전체 페이지 수", example = "3")
        int totalPages,
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
    public static NotificationPageResponse from(Page<Notification> page, List<NotificationResponse> notifications) {
        return new NotificationPageResponse(
                notifications,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
