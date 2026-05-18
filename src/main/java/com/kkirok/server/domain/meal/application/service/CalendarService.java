package com.kkirok.server.domain.meal.application.service;

import com.kkirok.server.domain.character.domain.CharacterStatusType;
import com.kkirok.server.domain.meal.application.dto.response.CalendarDayInfo;
import com.kkirok.server.domain.meal.application.dto.response.CalendarMonthInfo;
import com.kkirok.server.domain.meal.application.dto.response.CalendarMonthResponse;
import com.kkirok.server.domain.meal.application.dto.response.DailyMealResponse;
import com.kkirok.server.domain.meal.dao.MealRecordRepository;
import com.kkirok.server.domain.meal.domain.MealRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private final MealRecordRepository mealRecordRepository;

    /**
     * 월간 캘린더 조회
     * 해당 월의 날짜별로 식단 기록 유무에 따른 상태(CharacterStatusType)를 반환합니다.
     *
     * @param memberId       현재 로그인한 멤버 ID
     * @param year           조회할 연도
     * @param month          조회할 월 (1~12)
     * @param startDayOfWeek 시작 요일 (0:일 ~ 6:토)
     */
    public CalendarMonthResponse getMonthInfo(Long memberId, int year, int month, int startDayOfWeek) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 해당 월에 식단이 기록된 날짜 set
        Set<LocalDate> datesWithMeals = mealRecordRepository
                .findDatesWithMeals(memberId, startDate, endDate)
                .stream()
                .collect(Collectors.toSet());

        // 날짜별 status 목록 생성
        List<CalendarDayInfo> dayInfos = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            CharacterStatusType status = datesWithMeals.contains(cursor)
                    ? CharacterStatusType.ENERGETIC
                    : CharacterStatusType.NORMAL;
            dayInfos.add(new CalendarDayInfo(cursor.getDayOfMonth(), status));
            cursor = cursor.plusDays(1);
        }

        CalendarMonthInfo monthInfo = new CalendarMonthInfo(
                year,
                month,
                startDate.getDayOfMonth(),
                endDate.getDayOfMonth(),
                startDate.getDayOfWeek().getValue() % 7,  // Java DayOfWeek: MON=1 → 일=0 기준으로 변환
                endDate.getDayOfWeek().getValue() % 7
        );

        return new CalendarMonthResponse(dayInfos, monthInfo);
    }

    /**
     * 특정 날짜의 식단 상세 조회
     *
     * @param memberId 현재 로그인한 멤버 ID
     * @param date     조회할 날짜
     */
    public DailyMealResponse getDailyMealInfo(Long memberId, LocalDate date) {
        List<MealRecord> records = mealRecordRepository.getSpecifiedDateMealRecords(memberId, date);
        return DailyMealResponse.from(date, records);
    }
}
