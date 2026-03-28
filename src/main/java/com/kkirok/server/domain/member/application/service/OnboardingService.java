package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.application.dto.request.ProfileSettingRequest;
import com.kkirok.server.domain.member.application.usecase.MemberUseCase;
import com.kkirok.server.domain.member.domain.Member;
import com.kkirok.server.domain.member.exception.MemberErrorCode;
import com.kkirok.server.global.common.exception.BadRequestException;
import com.kkirok.server.global.external.r2.application.service.R2UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final MemberUseCase memberUseCase;
    private final R2UploadService r2UploadService;

    /*
        유저의 닉네임, 프로필이미지, 생년월일, 이름, 휴대폰번호, 온보딩 정보를 추가합니다.
        프로필 이미지 업로드 시 R2에 업로드합니다.
     */
    @Transactional
    public void updateProfile(Long memberId, ProfileSettingRequest request, MultipartFile profileImage) {

        // 요청 dto내의 습관 5개 넘는지 검증
        if (request.habits() != null && request.habits().size() > 5) {
            throw new BadRequestException(MemberErrorCode.ONBOARDING_HABIT_MAX_COUNT);
        }

        // 멤버 조회
        Member member = memberUseCase.findMemberByMemberId(memberId);

        // R2에 업로드 및 imageKey 반환
        String imageKey = extractProfileImageKey(profileImage);

        // Member와 Onboarding에 함께 적용
        member.updateOnboarding(request, imageKey);
        memberUseCase.updateMember(member);

    }

    private String extractProfileImageKey(final MultipartFile profileImage) {
        if (profileImage == null || profileImage.isEmpty()) {
            return null;
        }

        return r2UploadService.upload(profileImage);
    }

}
