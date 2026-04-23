package com.kkirok.server.domain.meal.application.service;

import com.kkirok.server.domain.meal.application.dto.request.MealCreateRequest;
import com.kkirok.server.domain.meal.application.dto.request.MealUpdateRequest;
import com.kkirok.server.domain.meal.application.dto.response.MealResponse;
import com.kkirok.server.domain.meal.application.usecase.MealRecordUseCase;
import com.kkirok.server.domain.meal.dao.MealRecordRepository;
import com.kkirok.server.domain.meal.domain.MealRecord;
import com.kkirok.server.domain.meal.domain.ScanType;
import com.kkirok.server.domain.meal.exception.MealErrorCode;
import com.kkirok.server.domain.meal.exception.MealException;
import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.dao.MemberRepository;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.global.common.exception.ForbiddenException;
import com.kkirok.server.global.common.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MealRecordService implements MealRecordUseCase {

    private final MealRecordRepository mealRecordRepository;
    private final MemberRepository memberRepository;
    private final MemberUseCase memberUseCase;

    @Override
    public List<MealRecord> getTodayRecords(Long memberId) {
        LocalDate today = LocalDate.now();
        return getRangeRecords(memberId, today, today.plusDays(1));
    }

    private List<MealRecord> getRangeRecords(Long memberId, LocalDate start, LocalDate end) {
        return mealRecordRepository.getSpecifiedDateMealRecords(
                memberId,
                start.atStartOfDay(),
                end.atStartOfDay()
        );
    }

    /** 직접 입력으로 식단 기록 */
    @Override
    @Transactional
    public MealResponse create(Long memberId, MealCreateRequest request) {
        Member member = memberUseCase.findMemberByMemberId(memberId);
        MealRecord meal = MealRecord.createManual(member, request);
        mealRecordRepository.save(meal);
        return MealResponse.fromManual(meal, request);
    }

    /** 카메라 촬영으로 식단 기록 (AI 분석) */
    @Override
    @Transactional
    public MealResponse createMealByCamera(Long memberId, String imageUrl) {
        Member member = memberUseCase.findMemberByMemberId(memberId);

        MealRecord meal = MealRecord.createByAi(member, ScanType.CAMERA);
        mealRecordRepository.save(meal);

        // TODO:
        // 1. imageUrl로 AI 분석 요청
        // 2. 분석 결과 저장
        // 3. meal.applyAiResult(...) 호출
        return MealResponse.from(meal);
    }

    /** 앨범 사진 업로드로 식단 기록 (AI 분석) */
    @Override
    @Transactional
    public MealResponse createMealByAlbum(Long memberId, MultipartFile file) {
        Member member = memberUseCase.findMemberByMemberId(memberId);

        if (file == null || file.isEmpty()) {
            throw new MealException(MealErrorCode.IMAGE_FILE_REQUIRED);
        }

        MealRecord meal = MealRecord.createByAi(member, ScanType.IMAGE);
        mealRecordRepository.save(meal);

        // TODO:
        // 1. file을 S3에 업로드해서 imageUrl 생성
        // 2. imageUrl로 AI 분석 요청
        // 3. 분석 결과 저장
        // 4. meal.applyAiResult(...) 호출
        return MealResponse.from(meal);
    }

    @Override
    @Transactional
    public MealResponse updateMeal(Long memberId, Long mealId, MealUpdateRequest request) {
        MealRecord meal = findMealWithOwnerCheck(memberId, mealId);
        meal.update(request);
        return MealResponse.fromUpdate(meal, request);
    }

    @Override
    @Transactional
    public void deleteMeal(Long memberId, Long mealId) {
        MealRecord meal = findMealWithOwnerCheck(memberId, mealId);
        mealRecordRepository.delete(meal);
    }

    public MealRecord findMealWithOwnerCheck(Long memberId, Long mealId) {
        MealRecord meal = mealRecordRepository.findByIdWithAnalyses(mealId)
                .orElseThrow(() -> new MealException(MealErrorCode.MEAL_NOT_FOUND));

        if (!meal.getMember().getId().equals(memberId)) {
            throw new ForbiddenException(MealErrorCode.MEAL_FORBIDDEN);
        }
        return meal;
    }
}