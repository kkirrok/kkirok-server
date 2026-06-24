package com.kkirok.server.domain.report.service;

import com.kkirok.server.domain.report.application.dao.WeeklyReportRepository;
import com.kkirok.server.domain.report.application.dto.request.WeeklyReportRequest;
import com.kkirok.server.domain.report.application.dto.response.WeeklyReportResponse;
import com.kkirok.server.domain.meal.domain.MealNutrition;
import com.kkirok.server.domain.meal.domain.MealRecord;
import com.kkirok.server.domain.meal.domain.MealTimeSlot;
import com.kkirok.server.global.external.openai.OpenAiService;
import com.kkirok.server.global.external.openai.dto.response.OpenAiWeeklyReportResult;
import com.kkirok.server.global.external.openai.prompt.PromptType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class WeeklyReportService {

    private final WeeklyReportRepository weeklyReportRepository;  // 수정
    private final OpenAiService openAiService;

    public WeeklyReportResponse getWeeklyReport(Long memberId, LocalDate weekStart) {
        // 1. 월~일 범위
        LocalDate monday = (weekStart != null) ? weekStart : getThisMonday();
        LocalDate sunday = monday.plusDays(6);

        // 2. 주간 식단 조회
        List<MealRecord> weeklyMeals = weeklyReportRepository.getWeeklyMealRecords(memberId, monday, sunday);  // 수정

        // 3. 집계
        WeeklyReportRequest input = buildInput(weeklyMeals);

        // 4. OpenAI 분석
        OpenAiWeeklyReportResult result = openAiService.createObjectResponse(
                PromptType.MEAL_WEEKLY_REPORT,
                input,
                OpenAiWeeklyReportResult.class
        );

        // 5. 응답 조립
        return toResponse(input, result);
    }

    private LocalDate getThisMonday() {
        return LocalDate.now().with(DayOfWeek.MONDAY);
    }

    private WeeklyReportRequest buildInput(List<MealRecord> meals) {
        Map<String, Integer> dailyKcals = new LinkedHashMap<>();
        for (DayOfWeek dow : DayOfWeek.values()) {
            dailyKcals.put(dow.name(), 0);
        }
        long recordedDays = meals.stream()
                .map(MealRecord::getMealDate)
                .distinct()
                .count();
        int days = recordedDays > 0 ? (int) recordedDays : 1;

        int    totalKcal          = 0;
        double totalProteinG      = 0.0;
        double totalCarbohydrateG = 0.0;
        double totalSugarG        = 0.0;
        double totalFatG          = 0.0;
        double totalSodiumMg      = 0.0;

        Map<String, Integer> slotCounts = new LinkedHashMap<>();
        for (MealTimeSlot slot : MealTimeSlot.values()) {
            slotCounts.put(slot.name(), 0);
        }

        for (MealRecord meal : meals) {
            MealNutrition n = meal.getMealNutrition();
            if (n != null) {
                totalKcal          += n.getKcal();
                totalProteinG      += n.getProteinG();
                totalCarbohydrateG += n.getCarbohydrateG();
                totalSugarG        += n.getSugarG();
                totalFatG          += n.getFatG();
                totalSodiumMg      += n.getSodiumMg();

                // 요일별 칼로리 누적
                String dow = meal.getMealDate().getDayOfWeek().name();
                dailyKcals.merge(dow, n.getKcal(), Integer::sum);
            }
            slotCounts.merge(meal.getMealTimeSlot().name(), 1, Integer::sum);
        }

        return new WeeklyReportRequest(
                totalKcal / days,
                Math.round(totalProteinG      / days * 10.0) / 10.0,
                Math.round(totalCarbohydrateG / days * 10.0) / 10.0,
                Math.round(totalSugarG / days * 10.0) / 10.0,
                Math.round(totalFatG          / days * 10.0) / 10.0,
                Math.round(totalSodiumMg      / days * 10.0) / 10.0,
                days,
                slotCounts,
                dailyKcals
        );
    }

    private WeeklyReportResponse toResponse(WeeklyReportRequest input, OpenAiWeeklyReportResult result) {
        List<WeeklyReportResponse.NutrientFeedback> nutrientFeedbacks = result.nutrientFeedbacks()
                .stream()
                .map(nf -> new WeeklyReportResponse.NutrientFeedback(
                        nf.nutrient(),
                        nf.avgMg() != null ? nf.avgMg() : (nf.avgG() != null ? nf.avgG() : 0.0),
                        nf.nutrient().equals("나트륨") ? "mg" : "g",
                        nf.status(),
                        nf.feedback()
                ))
                .toList();

        List<WeeklyReportResponse.Suggestion> suggestions = result.nextWeekSuggestions()
                .stream()
                .map(s -> new WeeklyReportResponse.Suggestion(s.title(), s.content()))
                .toList();

        int totalWeeklyKcal = input.dailyKcals().values().stream()
                .mapToInt(Integer::intValue).sum();

        return new WeeklyReportResponse(
                input.avgDailyKcal(),
                totalWeeklyKcal,
                input.dailyKcals(),
                result.kcalFeedback(),
                result.kcalStatus(),
                nutrientFeedbacks,
                input.slotCounts(),
                result.mealPatternDescription(),
                suggestions
        );
    }
}