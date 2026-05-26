package com.kkirok.server.global.common.api;

import com.kkirok.server.admin.domain.AdminRequestStatus;
import com.kkirok.server.domain.character.domain.CharacterStatusType;
import com.kkirok.server.domain.character.domain.ExpSourceType;
import com.kkirok.server.domain.character.domain.ItemType;
import com.kkirok.server.domain.kkinipop.domain.KkinipopGroupRole;
import com.kkirok.server.domain.kkinipop.domain.KkinipopReactionEmoji;
import com.kkirok.server.domain.meal.domain.DayPatternType;
import com.kkirok.server.domain.meal.domain.MealCategory;
import com.kkirok.server.domain.meal.domain.MealTimeSlot;
import com.kkirok.server.domain.meal.domain.ScanType;
import com.kkirok.server.domain.member.domain.AuthProvider;
import com.kkirok.server.domain.member.domain.Gender;
import com.kkirok.server.domain.member.domain.MealStyle;
import com.kkirok.server.domain.member.domain.OnboardingHabit;
import com.kkirok.server.domain.member.domain.OnboardingPurpose;
import com.kkirok.server.domain.member.domain.SocialType;
import com.kkirok.server.domain.member.domain.TendencySourceType;
import com.kkirok.server.domain.member.domain.TendencyType;
import com.kkirok.server.domain.notification.domain.DevicePlatform;
import com.kkirok.server.domain.report.domain.ReportItemType;
import com.kkirok.server.domain.report.domain.ReportType;
import com.kkirok.server.domain.user.domain.Role;
import com.kkirok.server.global.external.publicdata.dto.FoodSourceType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EnumList {

    ADMIN_REQUEST_STATUS(AdminRequestStatus.class, "관리자 계정 요청 상태"),
    ROLE(Role.class, "사용자 권한"),
    GENDER(Gender.class, "성별"),
    SOCIAL_TYPE(SocialType.class, "소셜 로그인 타입"),
    AUTH_PROVIDER(AuthProvider.class, "인증 제공자"),
    ONBOARDING_PURPOSE(OnboardingPurpose.class, "온보딩 목표"),
    ONBOARDING_HABIT(OnboardingHabit.class, "온보딩 식습관"),
    MEAL_STYLE(MealStyle.class, "식습관 유형"),
    MEMBER_NOTIFICATION_TYPE(com.kkirok.server.domain.member.domain.NotificationType.class, "회원 알림 동의 유형"),
    TENDENCY_TYPE(TendencyType.class, "식습관 성향"),
    TENDENCY_SOURCE_TYPE(TendencySourceType.class, "식습관 성향 출처"),
    MEAL_TIME_SLOT(MealTimeSlot.class, "식사 시간대"),
    MEAL_CATEGORY(MealCategory.class, "식사 카테고리"),
    SCAN_TYPE(ScanType.class, "이미지 스캔 타입"),
    DAY_PATTERN_TYPE(DayPatternType.class, "하루 식단 패턴"),
    ITEM_TYPE(ItemType.class, "캐릭터 아이템 타입"),
    CHARACTER_STATUS_TYPE(CharacterStatusType.class, "캐릭터 상태"),
    EXP_SOURCE_TYPE(ExpSourceType.class, "경험치 획득 출처"),
    KKINIPOP_GROUP_ROLE(KkinipopGroupRole.class, "끼니팝 그룹 역할"),
    KKINIPOP_REACTION_EMOJI(KkinipopReactionEmoji.class, "끼니팝 시스템 리액션 이모지"),
    DEVICE_PLATFORM(DevicePlatform.class, "디바이스 플랫폼"),
    NOTIFICATION_TYPE(com.kkirok.server.domain.notification.domain.NotificationType.class, "알림 유형"),
    REPORT_TYPE(ReportType.class, "리포트 타입"),
    REPORT_ITEM_TYPE(ReportItemType.class, "리포트 항목 타입"),
    FOOD_SOURCE_TYPE(FoodSourceType.class, "음식 영양 정보 출처")
    ;

    private final Class<?> enumClass;
    private final String description;

}
