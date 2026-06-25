package com.kkirok.server.domain.kkinipop.application.service;

import com.kkirok.server.domain.meal.application.usecase.MealRecordUseCase;
import com.kkirok.server.domain.meal.domain.ScanType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class KkinipopPersonalLogService {

    private final MealRecordUseCase mealRecordUseCase;

    // 이미지 분석 실패가 상위(끼니팝 게시글 저장) 트랜잭션에 영향을 주지 않도록 별도 트랜잭션에서 실행
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryRecordPersonalLog(Long memberId, MultipartFile image, ScanType scanType) {
        try {
            mealRecordUseCase.createMealByImage(memberId, image, scanType);
            return true;
        } catch (RuntimeException exception) {
            log.warn("나의 끼록 저장 실패 - memberId: {}", memberId, exception);
            return false;
        }
    }
}
