package com.kkirok.server.domain.meal.dao;

import com.kkirok.server.domain.meal.domain.MealRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MealRecordRepository extends JpaRepository<MealRecord, Long> {

    // 특정 날짜 식단 조회
    @Query("""
    SELECT DISTINCT mr
    FROM MealRecord mr
    LEFT JOIN FETCH mr.mealAiAnalyses
    LEFT JOIN FETCH mr.mealNutrition
    WHERE mr.member.id = :memberId
      AND mr.mealDate = :mealDate
    ORDER BY mr.recordedAt ASC
""")
    List<MealRecord> getSpecifiedDateMealRecords(
            @Param("memberId") Long memberId,
            @Param("mealDate") LocalDate mealDate
    );

    // 식단 단건 조회
    @Query("""
        SELECT mr FROM MealRecord mr
        LEFT JOIN FETCH mr.mealAiAnalyses
        LEFT JOIN FETCH mr.mealNutrition
        WHERE mr.id = :mealId
    """)
    Optional<MealRecord> findByIdWithAnalyses(@Param("mealId") Long mealId);

    // 특정 월에 식단이 기록된 날짜 목록 조회 (캘린더 dot 표시용)
    @Query("""
        SELECT DISTINCT mr.mealDate
        FROM MealRecord mr
        WHERE mr.member.id = :memberId
          AND mr.mealDate BETWEEN :startDate AND :endDate
        ORDER BY mr.mealDate ASC
    """)
    List<LocalDate> findDatesWithMeals(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        select new com.kkirok.server.domain.meal.dao.MealReminderRow(m.id, mr.id)
        from Member m
        join MealRecord mr on mr.member = m
        where mr.recordedAt = (
            select max(mr2.recordedAt) from MealRecord mr2 where mr2.member = m
        )
        and mr.recordedAt <= :threshold
        and m.deletedAt is null
        and m.onboardingCompleted = true
    """)
    List<MealReminderRow> findOverdueTargets(@Param("threshold") LocalDateTime threshold);

    @Query("""
        select m.id from Member m
        where m.deletedAt is null
          and m.onboardingCompleted = true
          and not exists (
              select 1 from MealRecord mr
              where mr.member = m and mr.mealDate = :today
          )
    """)
    List<Long> findMemberIdsWithoutMealOn(@Param("today") LocalDate today);
}