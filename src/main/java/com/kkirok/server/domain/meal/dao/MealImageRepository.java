package com.kkirok.server.domain.meal.dao;

import com.kkirok.server.domain.meal.domain.MealImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealImageRepository extends JpaRepository<MealImage, Long> {
}
