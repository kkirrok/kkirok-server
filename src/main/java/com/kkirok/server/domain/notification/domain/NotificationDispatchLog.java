package com.kkirok.server.domain.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "notification_dispatch_log",
        uniqueConstraints = @UniqueConstraint(name = "uk_notification_dispatch_log_key", columnNames = {"dispatch_key"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationDispatchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dispatch_key", nullable = false, unique = true, length = 100)
    private String dispatchKey;

    @Column(name = "dispatched_at", nullable = false)
    private LocalDateTime dispatchedAt;

    @Builder
    private NotificationDispatchLog(String dispatchKey, LocalDateTime dispatchedAt) {
        this.dispatchKey = dispatchKey;
        this.dispatchedAt = dispatchedAt;
    }

    public static NotificationDispatchLog of(NotificationType type, Long sourceId) {
        return NotificationDispatchLog.builder()
                .dispatchKey(createDispatchKey(type, sourceId))
                .dispatchedAt(LocalDateTime.now())
                .build();
    }

    public static String createDispatchKey(NotificationType type, Long sourceId) {
        return type.name() + ":" + sourceId;
    }
}
