package com.kkirok.server.domain.character.domain;

import com.kkirok.server.domain.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "character_status_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CharacterStatusHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "character_id", nullable = false)
    private Character character;

    @Column(name = "status_date", nullable = false)
    private LocalDate statusDate;

    @Column(name = "health_score", nullable = false)
    private Integer healthScore;

    @Builder
    private CharacterStatusHistory(Character character, LocalDate statusDate, Integer healthScore) {
        this.character = character;
        this.statusDate = statusDate;
        this.healthScore = healthScore;
    }

    public static CharacterStatusHistory create(Character character) {
        return CharacterStatusHistory.builder()
                .character(character)
                .statusDate(LocalDate.now())
                .healthScore(0)
                .build();
    }

}
