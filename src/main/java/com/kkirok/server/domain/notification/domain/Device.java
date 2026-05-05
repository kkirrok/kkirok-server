package com.kkirok.server.domain.notification.domain;

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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Table(
        name = "notification_device",
        uniqueConstraints = @UniqueConstraint(name = "uk_notification_device_token", columnNames = {"token"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DevicePlatform platform;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Builder
    private Device(Member member, String token, DevicePlatform platform, LocalDateTime lastUsedAt) {
        this.member = member;
        this.token = token;
        this.platform = platform;
        this.lastUsedAt = lastUsedAt;
    }

    public static Device create(Member member, String token, DevicePlatform platform, LocalDateTime now) {
        return Device.builder()
                .member(member)
                .token(token)
                .platform(platform)
                .lastUsedAt(now)
                .build();
    }

    public void touch(Member member, DevicePlatform platform, LocalDateTime now) {
        this.member = member;
        this.platform = platform;
        this.lastUsedAt = now;
    }
}
