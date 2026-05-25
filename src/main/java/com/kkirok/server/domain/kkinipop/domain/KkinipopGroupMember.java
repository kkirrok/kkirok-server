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
        name = "kkinipop_group_member",
        uniqueConstraints = @UniqueConstraint(name = "uk_kkinipop_group_member", columnNames = {"group_id", "member_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KkinipopGroupMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private KkinipopGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private KkinipopGroupRole role;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "banned", nullable = false)
    private boolean banned = false;

    @Builder
    private KkinipopGroupMember(KkinipopGroup group, Member member, KkinipopGroupRole role) {
        this.group = group;
        this.member = member;
        this.role = role;
    }

    public static KkinipopGroupMember createLeader(KkinipopGroup group, Member member) {
        return KkinipopGroupMember.builder()
                .group(group)
                .member(member)
                .role(KkinipopGroupRole.LEADER)
                .build();
    }

    public static KkinipopGroupMember createMember(KkinipopGroup group, Member member) {
        return KkinipopGroupMember.builder()
                .group(group)
                .member(member)
                .role(KkinipopGroupRole.MEMBER)
                .build();
    }

    public boolean isLeader() {
        return role == KkinipopGroupRole.LEADER;
    }

    public boolean isActive() {
        return leftAt == null;
    }

    public void leave(LocalDateTime leftAt) {
        this.leftAt = leftAt;
    }

    public void ban(LocalDateTime bannedAt) {
        this.leftAt = bannedAt;
        this.banned = true;
    }

    public boolean isBanned() {
        return banned;
    }
}
