package com.kkirok.server.domain.meal.domain;

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
import java.time.LocalDateTime;

import lombok.*;

@Entity
@Getter
@Builder // 이건 Builder로 생성. 필드가 너무 많음.
@Table(name = "meal_ai_analysis")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MealAiAnalysis extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_record_id", nullable = false)
    private MealRecord mealRecord;

    @Column(name = "detected_food_name", length = 100, nullable = false)
    private String detectedFoodName;

    @Column(name = "food_category", length = 100, nullable = false)
    private String foodCategory;

    @Column(name = "nutrition_summary", columnDefinition = "TEXT", nullable = false)
    private String nutritionSummary;

    @Column(name = "analyzed_at", nullable = false)
    @Builder.Default
    private LocalDateTime analyzedAt = LocalDateTime.now();

    @Column(name = "raw_result_json", columnDefinition = "TEXT")
    private String rawResultJson;

}
