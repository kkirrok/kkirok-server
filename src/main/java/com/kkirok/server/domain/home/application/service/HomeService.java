package com.kkirok.server.domain.home.application.service;

import com.kkirok.server.domain.home.application.dto.response.HomeResponse;
import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.domain.MealStyle;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.notification.application.policy.MealReminderPolicy;
import com.kkirok.server.domain.notification.application.service.MealReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 홈화면에 필요한 도메인 정보를 가져와 조합하여 반환합니다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HomeService {

    private final MealReminderService mealReminderService;
    private final MemberUseCase memberUseCase;
    private final List<MealReminderPolicy> mealReminderPolicies;

    public HomeResponse getHomeInfo(Long memberId) {

        // 데이터 조회
        String string = mealReminderService.toString();
        Member member = memberUseCase.findMemberByMemberId(memberId);

        // 멤버정보
        MealStyle mealStyle = member.getMealStyle();
        HomeResponse.HomeMemberInfo homeMemberInfo = new HomeResponse.HomeMemberInfo(mealStyle, mealStyle.getLabel(), member.getNickname());

        // 끼록 리마인더
        HomeResponse.HomeReminder homeReminder;
        MealReminderPolicy mealReminderPolicy = mealReminderPolicies.stream() // 내가 타겟이 되는 리마인더 있으면 그걸 이용
                .filter(policy -> policy.isImTarget(LocalDateTime.now(), memberId))
                .findFirst()
                .orElse(null);
        if(mealReminderPolicy != null) {

        }


        // 조립 후 반환
        return new HomeResponse(
                null,
                null,
                null,
                null
        );
    }
}
