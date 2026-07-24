package com.kkirok.server.domain.member.application.usecase;

// 유저의 권장 칼로리를 수정/조회하는 usecase
public interface MemberNutritionUseCase {
    void updateKcal(Long memberId, int kcal);
    Integer getSuggestedKcal(Long memberId);
}
