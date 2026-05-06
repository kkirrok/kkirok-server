package com.kkirok.server.domain.meal.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@Table(name = "meal_nutrition")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MealNutrition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_record_id", nullable = false)
    private MealRecord mealRecord;

    @Column(nullable = false)
    private Integer kcal;

    @Column(name = "protein_g", nullable = false)
    @Builder.Default
    private Double proteinG = 0.0;

    @Column(name = "carbohydrate_g", nullable = false)
    @Builder.Default
    private Double carbohydrateG = 0.0;

    @Column(name = "sugar_g", nullable = false)
    @Builder.Default
    private Double sugarG = 0.0;

    @Column(name = "fat_g", nullable = false)
    @Builder.Default
    private Double fatG = 0.0;

    @Column(name = "sodium_mg", nullable = false)
    @Builder.Default
    private Double sodiumMg = 0.0;

    public static MealNutrition create(
            MealRecord mealRecord,
            Integer kcal,
            Double proteinG,
            Double carbohydrateG,
            Double sugarG,
            Double fatG,
            Double sodiumMg
    ) {
        return MealNutrition.builder()
                .mealRecord(mealRecord)
                .kcal(kcal != null ? kcal : 0)
                .proteinG(proteinG != null ? proteinG : 0.0)
                .carbohydrateG(carbohydrateG != null ? carbohydrateG : 0.0)
                .sugarG(sugarG != null ? sugarG : 0.0)
                .fatG(fatG != null ? fatG : 0.0)
                .sodiumMg(sodiumMg != null ? sodiumMg : 0.0)
                .build();
    }
    //
    public void update(Integer kcal, Double proteinG, Double carbohydrateG,
                       Double sugarG, Double fatG, Double sodiumMg) {
        this.kcal = kcal != null ? kcal : 0;
        this.proteinG = proteinG != null ? proteinG : 0.0;
        this.carbohydrateG = carbohydrateG != null ? carbohydrateG : 0.0;
        this.sugarG = sugarG != null ? sugarG : 0.0;
        this.fatG = fatG != null ? fatG : 0.0;
        this.sodiumMg = sodiumMg != null ? sodiumMg : 0.0;
    }

}