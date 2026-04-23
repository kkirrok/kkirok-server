package com.kkirok.server.domain.kkinipop.domain;

import com.kkirok.server.domain.kkinipop.application.dto.request.KkinipopGroupCreateRequest;
import com.kkirok.server.domain.BaseTimeEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "kkinipop_group")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KkinipopGroup extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String name;

    @Column(name = "invite_code", nullable = false, unique = true, length = 12)
    private String inviteCode;

    @Column(nullable = false)
    private int level;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KkinipopGroupMember> memberships = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KkinipopMission> missions = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KkinipopPost> posts = new ArrayList<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KkinipopCustomEmoji> customEmojis = new ArrayList<>();

    @Builder
    private KkinipopGroup(String name, String inviteCode, int level) {
        this.name = name;
        this.inviteCode = inviteCode;
        this.level = level;
    }

    public static KkinipopGroup create(KkinipopGroupCreateRequest request, String inviteCode) {
        return KkinipopGroup.builder()
                .name(request.name().trim())
                .inviteCode(inviteCode)
                .level(1)
                .build();
    }

    public void delete(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
