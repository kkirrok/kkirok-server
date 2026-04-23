package com.kkirok.server.domain.kkinipop.domain;
import com.kkirok.server.domain.BaseTimeEntity;
import com.kkirok.server.domain.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "kkinipop_custom_emoji")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KkinipopCustomEmoji extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private KkinipopGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member creator;

    @Column(nullable = false, length = 20)
    private String label;

    @Column(name = "image_key", nullable = false, length = 255)
    private String imageKey;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    private KkinipopCustomEmoji(KkinipopGroup group, Member creator, String label, String imageKey) {
        this.group = group;
        this.creator = creator;
        this.label = label;
        this.imageKey = imageKey;
    }

    public static KkinipopCustomEmoji create(
            KkinipopGroupMember groupMember,
            String label,
            String imageKey
    ) {
        return KkinipopCustomEmoji.builder()
                .group(groupMember.getGroup())
                .creator(groupMember.getMember())
                .label(label)
                .imageKey(imageKey)
                .build();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void delete(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
