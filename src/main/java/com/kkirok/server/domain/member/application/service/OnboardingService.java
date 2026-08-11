package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.application.dto.request.ProfileSettingRequest;
import com.kkirok.server.domain.member.application.dto.response.OnboardingProfileResponse;
import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.domain.MealStyle;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.member.domain.OnboardingHabit;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.domain.user.application.service.UserRoleService;
import com.kkirok.server.global.auth.jwt.application.RoleCacheService;
import com.kkirok.server.global.common.exception.BadRequestException;
import com.kkirok.server.global.external.r2.application.service.R2UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final MemberUseCase memberUseCase;
    private final R2UploadService r2UploadService;
    private final UserRoleService userRoleService;
    private final MealStyleClassificationService mealStyleClassificationService;
    private final RoleCacheService roleCacheService;

    /*
        유저의 닉네임, 프로필이미지, 생년월일, 이름, 휴대폰번호, 온보딩 정보를 추가합니다.
        프로필 이미지 업로드 시 R2에 업로드합니다.
     */
    @Transactional
    public void updateProfile(Long memberId, ProfileSettingRequest request, MultipartFile profileImage) {

        // 요청 dto내 값 검증
        // 식습관 ( 1개 이상 5개 이하 )
        if (request.habits() == null || request.habits().size() > 5 || request.habits().isEmpty()) {
            throw new BadRequestException(MemberErrorCode.INVALID_HABIT_INFO);
        }

        // 목표 ( 필수값 )
        if (request.purpose() == null) {
            throw new BadRequestException(MemberErrorCode.INVALID_PURPOSE_INFO);
        }

        // 멤버 조회
        Member member = memberUseCase.findMemberByMemberId(memberId);

        // R2에 업로드 및 imageKey 반환
        String imageKey = extractProfileImageKey(profileImage);

        // Member와 Onboarding에 함께 적용
        member.updateOnboarding(request, imageKey);
        userRoleService.promoteToUser(member.getUser());
        roleCacheService.evictRole(member.getId());
        memberUseCase.updateMember(member);

    }

    public void assignMealStyle(Long memberId, List<OnboardingHabit> habits) {
        MealStyle mealStyle = mealStyleClassificationService.classify(habits);
        memberUseCase.updateMealStyle(memberId, mealStyle);
    }

    /*
        프로필 설정, 온보딩 화면 조회를 위한 메서드
     */
    @Transactional(readOnly = true)
    public OnboardingProfileResponse getOnboardingInfo(Long memberId) {
        Member member = memberUseCase.findWithOnboarding(memberId);
        return OnboardingProfileResponse.of(member);
    }

    private String extractProfileImageKey(final MultipartFile profileImage) {
        if (profileImage == null || profileImage.isEmpty()) {
            return null;
        }

        return r2UploadService.upload(profileImage);
    }

    // 휴대전화 번호 규격에 맞는지
    private void checkPhoneValidation(String phone){

        Pattern PHONE_PATTERN = Pattern.compile("^010-\\d{4}-\\d{4}$"); // 010-1111-1111 규격

        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new BadRequestException(MemberErrorCode.INVALID_PHONE_FORMAT);
        }
    }

}
