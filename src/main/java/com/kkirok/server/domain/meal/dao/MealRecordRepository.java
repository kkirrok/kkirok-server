package com.kkirok.server.domain.meal.dao;

import com.kkirok.server.domain.meal.domain.MealRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MealRecordRepository extends JpaRepository<MealRecord, Long> {
    // 오늘 식단 조회
    @Query("""
        SELECT DISTINCT mr
        FROM MealRecord mr
        LEFT JOIN FETCH mr.mealAiAnalyses
        WHERE mr.member.id = :memberId
          AND mr.createdAt >= :startOfDay
          AND mr.createdAt < :endOfDay
        ORDER BY mr.createdAt DESC
""")
    List<MealRecord> getSpecifiedDateMealRecords(
            @Param("memberId") Long memberId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    // 식단 단건 조회
    @Query("""
        SELECT mr FROM MealRecord mr
        LEFT JOIN FETCH mr.mealAiAnalyses
        WHERE mr.id = :mealId
    """)
    Optional<MealRecord> findByIdWithAnalyses(@Param("mealId") Long mealId);
}
