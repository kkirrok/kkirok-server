package com.kkirok.server.domain.kkinipop.application.service;

import com.kkirok.server.domain.meal.application.usecase.MealRecordUseCase;
import com.kkirok.server.domain.meal.domain.ScanType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class KkinipopPersonalLogService {

    private final MealRecordUseCase mealRecordUseCase;

    // 나의끼록 저장 실패(이미지 이상 등)는 게시글 저장 자체를 막아야 하므로 예외를 흡수하지 않고
    // 상위(KkinipopPostService.createPost)로 그대로 전파합니다.
    // REQUIRES_NEW로 별도 트랜잭션을 갖지만, 예외 전파 시 두 트랜잭션 모두 롤백됩니다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordPersonalLog(Long memberId, MultipartFile image, ScanType scanType) {
        mealRecordUseCase.createMealByImage(memberId, image, scanType);
    }
}
