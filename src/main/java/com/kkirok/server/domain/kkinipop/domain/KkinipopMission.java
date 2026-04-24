package com.kkirok.server.domain.kkinipop.domain;

import com.kkirok.server.domain.BaseTimeEntity;
import com.kkirok.server.domain.kkinipop.application.dto.response.KkinipopMissionGenerateResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "kkinipop_mission")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KkinipopMission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private KkinipopGroup group;

    @Column(nullable = false, length = 40)
    private String title;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Builder
    private KkinipopMission(
            KkinipopGroup group,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        this.group = group;
        this.title = title;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public static KkinipopMission create(
            KkinipopGroup group,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        return KkinipopMission.builder()
                .group(group)
                .title(title)
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }

    public static KkinipopMission create(KkinipopGroup group, LocalDate targetDate, KkinipopMissionGenerateResponse.MissionCandidate candidate){
        LocalDateTime startAt = targetDate.atTime(candidate.startTime());
        return KkinipopMission.create(
                group,
                candidate.title(),
                startAt,
                startAt.plusMinutes(candidate.durationMinutes())
        );
    }

    public boolean isLive(LocalDateTime now) {
        return startAt.isBefore(now) && endAt.isAfter(now) && closedAt == null;
    }

    public boolean isEnded(LocalDateTime now) {
        return closedAt != null || !endAt.isAfter(now);
    }

    public long getDurationMinutes() {
        return Duration.between(startAt, endAt).toMinutes();
    }

    public long getRemainingSeconds(LocalDateTime now) {
        if (!isLive(now)) {
            return 0L;
        }
        return Duration.between(now, endAt).toSeconds();
    }

    public void close(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }
}
