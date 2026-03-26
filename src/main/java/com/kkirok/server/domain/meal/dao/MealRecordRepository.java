package com.kkirok.server.domain.meal.dao;

import com.kkirok.server.domain.meal.domain.MealRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MealRecordRepository extends JpaRepository<MealRecord, Long> {

    @Query("""
        SELECT mr FROM MealRecord as mr
        LEFT JOIN FETCH MealAiAnalysis as mai
            ON mai.mealRecord.id = mr.id
        WHERE mr.member.id = :memberId
            AND mr.createdAt >= :startOfDay
            AND mr.createdAt < :endOfDay
            AND mr.aiAnalyzed IS TRUE
    """)
    List<MealRecord> getSpecifiedDateMealRecords( // 특정 일시에 대해 MealRecord 목록 조회
            @Param("memberId") Long memberId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

}
