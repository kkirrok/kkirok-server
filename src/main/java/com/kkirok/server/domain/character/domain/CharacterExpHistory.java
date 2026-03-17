package com.kkirok.server.domain.character.domain;

import com.kkirok.server.domain.BaseTimeEntity;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "character_exp_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CharacterExpHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    @Column(name = "exp_amount", nullable = false)
    private Integer expAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpSourceType reason;

    @Builder
    private CharacterExpHistory(Character character, Integer expAmount, ExpSourceType reason) {
        this.character = character;
        this.expAmount = expAmount;
        this.reason = reason;
    }

    public static CharacterExpHistory create(Character character, Integer expAmount, ExpSourceType reason) {
        return CharacterExpHistory.builder()
                .character(character)
                .expAmount(expAmount)
                .reason(reason)
                .build();
    }

}
