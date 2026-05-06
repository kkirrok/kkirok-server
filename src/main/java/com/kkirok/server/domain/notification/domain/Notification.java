package com.kkirok.server.domain.notification.domain;

import com.kkirok.server.domain.BaseTimeEntity;
import com.kkirok.server.domain.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Table(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, length = 255)
    private String body;

    @Column(name = "data_json", columnDefinition = "text")
    private String dataJson;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    private Notification(Member member, NotificationType type, String title, String body, String dataJson) {
        this.member = member;
        this.type = type;
        this.title = title;
        this.body = body;
        this.dataJson = dataJson;
    }

    public static Notification create(Member member, NotificationType type, String title, String body, String dataJson) {
        return new Notification(member, type, title, body, dataJson);
    }

    public void markRead(LocalDateTime now) {
        this.readAt = now;
    }

    public void markSent(LocalDateTime now) {
        this.sentAt = now;
        this.failedAt = null;
        this.failureReason = null;
    }

    public void markFailed(LocalDateTime now, String reason) {
        this.failedAt = now;
        this.failureReason = reason;
    }
}
