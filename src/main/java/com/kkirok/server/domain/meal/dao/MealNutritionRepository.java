package com.kkirok.server.domain.meal.dao;

import com.kkirok.server.domain.meal.domain.MealNutrition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealNutritionRepository extends JpaRepository<MealNutrition, Long> {
}
