package com.kkirok.server.domain.notification.application.dto.response;

import com.kkirok.server.domain.notification.domain.Notification;
import com.kkirok.server.domain.notification.domain.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "알림 응답")
public record NotificationResponse(
        @Schema(description = "알림 ID", example = "1")
        Long notificationId,
        @Schema(description = "알림 타입", example = "GROUP_JOIN")
        NotificationType type,
        @Schema(description = "알림 제목", example = "홍길동님이 '아침 습관 챌린지'에 참여하셨습니다.")
        String title,
        @Schema(description = "알림 본문", example = "홍길동 님이 아침 습관 챌린지 그룹에 참여했어요.")
        String body,
        @Schema(
                description = "추가 데이터. 알림 타입별로 키 구성이 다르며, 클라이언트가 딥링크, 화면 라우팅, 아이콘/리소스 매핑에 사용합니다. "
                        + "예: GROUP_JOIN={type, groupId, joinedMemberId}, MISSION_START={type, groupId, missionId}, "
                        + "KKINIPOP_REACTION={type, postId, groupId, reactorMemberId, emojiCode, isCustom, customEmojiImageKey?}",
                example = "{\"type\":\"KKINIPOP_REACTION\",\"postId\":\"30\",\"groupId\":\"10\",\"reactorMemberId\":\"2\",\"emojiCode\":\"CUSTOM_5\",\"isCustom\":\"true\",\"customEmojiImageKey\":\"uuid_emoji\"}"
        )
        Map<String, String> data,
        @Schema(description = "발송 시각", nullable = true, example = "2026-05-05T08:55:00")
        LocalDateTime sentAt,
        @Schema(description = "읽음 여부", example = "false")
        boolean isRead,
        @Schema(description = "알림 미리보기 이미지 키", example = "imagekey")
        String image
) {

        public static NotificationResponse from(Notification notification, Map<String, String> parseData) {
                return new NotificationResponse(
                        notification.getId(),
                        notification.getType(),
                        notification.getTitle(),
                        notification.getBody(),
                        parseData,
                        notification.getSentAt(),
                        notification.getIsRead(),
                        notification.getImage()
                );
        }

}
