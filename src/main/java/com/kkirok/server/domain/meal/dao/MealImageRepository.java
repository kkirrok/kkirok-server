package com.kkirok.server.domain.meal.dao;

import com.kkirok.server.domain.meal.domain.MealImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MealImageRepository extends JpaRepository<MealImage, Long> {

    Optional<MealImage> findFirstByMealRecordId(Long mealRecordId);

    List<MealImage> findAllByMealRecordIdIn(List<Long> mealRecordIds);
}