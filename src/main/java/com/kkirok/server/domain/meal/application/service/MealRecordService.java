package com.kkirok.server.domain.meal.application.service;

import com.kkirok.server.domain.meal.application.usecase.MealRecordUseCase;
import com.kkirok.server.domain.meal.dao.MealRecordRepository;
import com.kkirok.server.domain.meal.domain.MealRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MealRecordService implements MealRecordUseCase {

    private final MealRecordRepository mealRecordRepository;

    @Override
    public List<MealRecord> getTodayRecords(Long memberId, LocalDate date) {

        List<MealRecord> todayMealRecords = mealRecordRepository.getSpecifiedDateMealRecords(
                memberId,
                date.atStartOfDay(),
                date.plusDays(1).atStartOfDay()
        );

    }



}
