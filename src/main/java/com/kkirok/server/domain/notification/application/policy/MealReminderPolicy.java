package com.kkirok.server.domain.notification.application.policy;

import com.kkirok.server.domain.notification.domain.NotificationType;
import java.time.LocalDateTime;
import java.util.List;

public interface MealReminderPolicy {
    NotificationType type();

    String title();

    String body();

    List<MealReminderTarget> findTargets(LocalDateTime now);

    default Boolean isImTarget(LocalDateTime now, Long memberId) {
        return findTargets(now).stream()
                .anyMatch(target -> target.memberId().equals(memberId));
    }

}
