package com.kkirok.server.domain.kkinipop.domain;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "kkinipop_post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KkinipopPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private KkinipopGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id")
    private KkinipopMission mission;

    @OneToMany(mappedBy = "post", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<KkinipopReaction> reactions = new ArrayList<>();

    @Column(name = "image_key", nullable = false, length = 255)
    private String imageKey;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "save_to_personal_log", nullable = false)
    private boolean saveToPersonalLog;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private KkinipopPost(
            KkinipopGroup group,
            Member member,
            KkinipopMission mission,
            String imageKey,
            LocalDate recordDate,
            boolean saveToPersonalLog
    ) {
        this.group = group;
        this.member = member;
        this.mission = mission;
        this.imageKey = imageKey;
        this.recordDate = recordDate;
        this.saveToPersonalLog = saveToPersonalLog;
    }

    public static KkinipopPost create(
            KkinipopGroupMember groupMember,
            KkinipopMission mission,
            String imageKey,
            LocalDate recordDate,
            boolean saveToPersonalLog
    ) {
        return KkinipopPost.builder()
                .group(groupMember.getGroup())
                .member(groupMember.getMember())
                .mission(mission)
                .imageKey(imageKey)
                .recordDate(recordDate)
                .saveToPersonalLog(saveToPersonalLog)
                .build();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void delete(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public void cancelPersonalLogSave() {
        this.saveToPersonalLog = false;
    }
}
