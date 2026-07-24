package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.meal.application.usecase.RecommendedAmountUseCase;
import com.kkirok.server.domain.member.application.dto.response.MyPageResponse;
import com.kkirok.server.domain.member.application.usecase.MemberNutritionUseCase;
import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService implements MemberNutritionUseCase {

    private final MemberUseCase memberUseCase;
    private final RecommendedAmountUseCase recommendedAmountUseCase;

    public MyPageResponse myPage(Long memberId) {

        Member member = memberUseCase.findMemberByMemberId(memberId);
        int recommendedKcal = recommendedAmountUseCase.getRecommendedKcal(memberId);

        return MyPageResponse.of(member, recommendedKcal);
    }

    @Override
    @Transactional
    public void updateKcal(Long memberId, int kcal) {
        Member member = memberUseCase.findMemberByMemberId(memberId);
        member.updateSuggestedKcal(kcal);
    }

    @Override
    public Integer getSuggestedKcal(Long memberId) {
        Member member = memberUseCase.findMemberByMemberId(memberId);
        return member.getSuggestedKcal();
    }
}
