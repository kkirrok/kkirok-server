package com.kkirok.server.domain.meal.application.service;

import com.kkirok.server.domain.meal.application.dto.response.YesterdayPickResponse;
import com.kkirok.server.domain.meal.dao.MealImageRepository;
import com.kkirok.server.domain.meal.dao.MealRecordRepository;
import com.kkirok.server.domain.meal.domain.MealImage;
import com.kkirok.server.domain.meal.domain.MealRecord;
import com.kkirok.server.domain.meal.domain.MealTimeSlot;
import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.domain.MealStyle;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.global.external.r2.application.service.PresignedUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityMealService {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final int PICK_LIMIT = 4;

    private final MealRecordRepository mealRecordRepository;
    private final MealImageRepository mealImageRepository;
    private final MemberUseCase memberUseCase;
    private final PresignedUrlService presignedUrlService;

    /**
     * 같은 MealStyle 유저의 어제 이 시간대 픽을 반환합니다.
     *
     * <p>현재 시각을 기준으로 시간대 슬롯(아침/점심/저녁/간식)을 판단하고,
     * 같은 MealStyle을 가진 다른 유저들이 어제 같은 슬롯에 기록한 식사를
     * 랜덤 최대 4개 반환합니다.</p>
     */
    public YesterdayPickResponse getYesterdayPicks(Long memberId) {
        Member member = memberUseCase.findMemberByMemberId(memberId);
        MealStyle mealStyle = member.getMealStyle() != null
                ? member.getMealStyle()
                : MealStyle.BALANCED;

        LocalDate yesterday = LocalDate.now(KOREA_ZONE).minusDays(1);
        MealTimeSlot currentSlot = resolveCurrentTimeSlot();

        List<MealRecord> records = mealRecordRepository.findYesterdayPicksBySameStyle(
                mealStyle,
                memberId,
                yesterday,
                currentSlot,
                PageRequest.of(0, PICK_LIMIT)
        );

        List<Long> recordIds = records.stream().map(MealRecord::getId).toList();
        Map<Long, String> imageKeyByRecordId = mealImageRepository.findAllByMealRecordIdIn(recordIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        img -> img.getMealRecord().getId(),
                        MealImage::getImage,
                        (a, b) -> a  // 이미지 여러 장이면 첫 번째만
                ));

        List<YesterdayPickResponse.PickItem> picks = records.stream()
                .map(record -> {
                    String imageKey = imageKeyByRecordId.get(record.getId());
                    String imageUrl = null;
                    if (imageKey != null) {
                        try {
                            imageUrl = presignedUrlService.getPresignedUrl(imageKey).toString();
                        } catch (Exception ignored) {}
                    }
                    Integer kcal = record.getMealNutrition() != null
                            ? record.getMealNutrition().getKcal()
                            : null;
                    return new YesterdayPickResponse.PickItem(
                            record.getId(),
                            record.getName(),
                            kcal,
                            imageUrl
                    );
                })
                .toList();

        return new YesterdayPickResponse(mealStyle, mealStyle.getLabel(), currentSlot, picks);
    }

    /**
     * 현재 시각(KST)을 기준으로 식사 시간대 슬롯을 결정합니다.
     * 06:00 ~ 10:00 → 아침
     * 10:00 ~ 15:00 → 점심
     * 15:00 ~ 20:00 → 저녁
     * 그 외         → 간식
     */
    private MealTimeSlot resolveCurrentTimeSlot() {
        LocalTime now = LocalTime.now(KOREA_ZONE);
        if (!now.isBefore(LocalTime.of(6, 0)) && now.isBefore(LocalTime.of(10, 0))) {
            return MealTimeSlot.BREAKFAST;
        } else if (!now.isBefore(LocalTime.of(10, 0)) && now.isBefore(LocalTime.of(15, 0))) {
            return MealTimeSlot.LUNCH;
        } else if (!now.isBefore(LocalTime.of(15, 0)) && now.isBefore(LocalTime.of(20, 0))) {
            return MealTimeSlot.DINNER;
        } else {
            return MealTimeSlot.SNACK;
        }
    }
}