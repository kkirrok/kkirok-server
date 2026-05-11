package com.kkirok.server.domain.report.application.dao;

import com.kkirok.server.domain.meal.domain.MealRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WeeklyReportRepository extends JpaRepository<MealRecord, Long> {

    @Query("""
        SELECT DISTINCT mr
        FROM MealRecord mr
        LEFT JOIN FETCH mr.mealNutrition
        WHERE mr.member.id = :memberId
          AND mr.mealDate >= :startDate
          AND mr.mealDate <= :endDate
        ORDER BY mr.mealDate ASC, mr.recordedAt ASC
    """)
    List<MealRecord> getWeeklyMealRecords(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}