package com.kkirok.server.domain.notification.domain;

import com.kkirok.server.domain.member.domain.NotificationAgreeType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    MEAL_REMINDER_OVERDUE(NotificationAgreeType.KKIROK),
    MEAL_REMINDER_NO_TODAY(NotificationAgreeType.KKIROK),

    GROUP_JOIN(NotificationAgreeType.GROUP_JOIN_AND_QUIT),

    MISSION_START(NotificationAgreeType.KKINIPOP),

    KKINIPOP_REACTION(NotificationAgreeType.REACTION);

    private final NotificationAgreeType agreeType;
}
