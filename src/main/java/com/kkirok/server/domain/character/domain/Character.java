package com.kkirok.server.domain.character.domain;

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

import lombok.*;

@Entity
@Getter
@Table(name = "character")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Character extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_type_id", nullable = false)
    private CharacterType characterType;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer level;

    @Column(nullable = false)
    private Integer exp;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_status", nullable = false, length = 30)
    private CharacterStatusType currentStatus;

    @Column(name = "current_health_score", precision = 5, scale = 2, nullable = false)
    private Long currentHealthScore;

    @Column(name = "last_status_updated_at", nullable = false)
    private LocalDateTime lastStatusUpdatedAt;

    @Builder
    private Character(Member member, CharacterType characterType, String name, CharacterStatusType currentStatus) {
        this.member = member;
        this.characterType = characterType;
        this.name = name;
        this.level = 1;
        this.exp = 0;
        this.currentStatus = currentStatus != null ? currentStatus : CharacterStatusType.NORMAL;
        this.currentHealthScore = 0L;
        this.lastStatusUpdatedAt = LocalDateTime.now();
    }

    public static Character create(){
        return Character.builder() // TODO: create() 메서드 완성
                .build();
    }

}
