package com.kkirok.server.domain.meal.domain;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "meal_record")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MealRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_time_slot", nullable = false)
    private MealTimeSlot mealTimeSlot;

    @Column(length = 50, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_category", nullable = false)
    private MealCategory category;

    @Column(name = "is_ai_analyzed", nullable = false)
    private boolean aiAnalyzed;

    @Enumerated(EnumType.STRING)
    @Column(name = "scan_type", nullable = false)
    private ScanType scanType;

    @Column(name = "health_score")
    private Integer healthScore;

    @Column(columnDefinition = "TEXT")
    private String memo;

    @Builder
    private MealRecord(Member member, LocalDateTime recordedAt, LocalDate mealDate, MealTimeSlot mealTimeSlot,
                       String name, MealCategory category, boolean aiAnalyzed, ScanType scanType,
                       Integer healthScore, String memo){
        this.member = member;
        this.recordedAt = recordedAt;
        this.mealDate = mealDate;
        this.mealTimeSlot = mealTimeSlot;
        this.name = name;
        this.category = category;
        this.aiAnalyzed = aiAnalyzed;
        this.scanType = scanType;
        this.healthScore = healthScore;
        this.memo = memo;
    }

    public static MealRecord create(Member member) { // TODO: Member와 요청 DTO 받아서 하도록 수정
        return MealRecord.builder()
                .member(member)
                .build();
    }



}
