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

    // 오늘의 MealRecord 반환
    @Override
    public List<MealRecord> getTodayRecords(Long memberId) {

        LocalDate today = LocalDate.now();

        // today, today+1을 파라미터로 넣어 오늘만 조회
        return getRangeRecords(memberId, today, today.plusDays(1));
    }

    // start 이상 end 미만 범위의 MealRecords를 반환
    private List<MealRecord> getRangeRecords(Long memberId, LocalDate start, LocalDate end) {
        return mealRecordRepository.getSpecifiedDateMealRecords(
                memberId,
                start.atStartOfDay(),
                end.atStartOfDay()
        );
    }



}
