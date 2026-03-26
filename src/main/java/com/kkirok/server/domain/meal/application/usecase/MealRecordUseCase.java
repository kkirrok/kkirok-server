package com.kkirok.server.domain.meal.application.usecase;

import com.kkirok.server.domain.meal.domain.MealRecord;

import java.time.LocalDate;
import java.util.List;

public interface MealRecordUseCase {

    public List<MealRecord> getTodayRecords(Long memberId);

}
