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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "meal_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MealImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_record_id", nullable = false)
    private MealRecord mealRecord;

    @Column(nullable = false, length = 255)
    private String image;

    @Builder
    private MealImage (MealRecord mealRecord, String image) {
        this.mealRecord = mealRecord;
        this.image = image;
    }

    public static MealImage create(MealRecord mealRecord, String image) {
        return MealImage.builder()
                .mealRecord(mealRecord)
                .image(image)
                .build();
    }

}
