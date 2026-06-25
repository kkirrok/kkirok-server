package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.meal.application.usecase.RecommendedAmountUseCase;
import com.kkirok.server.domain.member.application.dto.response.MyPageResponse;
import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final MemberUseCase memberUseCase;
    private final RecommendedAmountUseCase recommendedAmountUseCase;

    public MyPageResponse myPage(Long memberId) {

        Member member = memberUseCase.findMemberByMemberId(memberId);
        int recommendedKcal = recommendedAmountUseCase.getRecommendedKcal(memberId);

        return MyPageResponse.of(member, recommendedKcal);
    }

}
