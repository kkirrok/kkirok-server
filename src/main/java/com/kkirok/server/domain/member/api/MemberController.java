package com.kkirok.server.domain.member.api;

import com.kkirok.server.domain.member.application.dto.request.FindEmailRequest;
import com.kkirok.server.domain.member.application.dto.request.ProfileSettingRequest;
import com.kkirok.server.domain.member.application.dto.request.ResetPasswordRequest;
import com.kkirok.server.domain.member.application.dto.response.FoundEmailResponse;
import com.kkirok.server.domain.member.application.dto.response.MyPageResponse;
import com.kkirok.server.domain.member.application.dto.response.OnboardingOptionResponse;
import com.kkirok.server.domain.member.application.dto.response.OnboardingProfileResponse;
import com.kkirok.server.domain.member.application.service.AccountRecoveryService;
import com.kkirok.server.domain.member.application.service.MemberService;
import com.kkirok.server.domain.member.application.service.MyPageService;
import com.kkirok.server.domain.member.application.service.OnboardingService;
import com.kkirok.server.domain.member.domain.OnboardingHabit;
import com.kkirok.server.domain.member.domain.OnboardingPurpose;
import com.kkirok.server.domain.member.exception.MemberSuccessCode;
import com.kkirok.server.domain.user.domain.Role;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.auth.annotation.RoleAuth;
import com.kkirok.server.global.auth.annotation.RoleUserAuth;
import com.kkirok.server.global.common.dto.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class MemberController implements MemberApi {

    private final AccountRecoveryService accountRecoveryService;
    private final OnboardingService onboardingService;
    private final MemberService memberService;
    private final MyPageService myPageService;

    @Override
    @PostMapping("/recovery/email")
    public ResponseEntity<SuccessResponse<FoundEmailResponse>> findEmail(
            @Valid @RequestBody final FindEmailRequest request
    ) {
        FoundEmailResponse response = FoundEmailResponse.of(accountRecoveryService.findEmail(request));
        return ResponseEntity.ok().body(SuccessResponse.of(MemberSuccessCode.FIND_EMAIL_SUCCESS, response));
    }

    @Override
    @PostMapping("/recovery/password")
    @RoleUserAuth
    public ResponseEntity<SuccessResponse<Void>> resetPassword(
            @CurrentMember final Long memberId,
            @Valid @RequestBody final ResetPasswordRequest request
    ) {
        accountRecoveryService.resetPassword(memberId, request);
        return ResponseEntity.ok().body(SuccessResponse.from(MemberSuccessCode.RESET_PASSWORD_SUCCESS));
    }

    @Override
    @GetMapping("/profile")
    @RoleUserAuth
    public ResponseEntity<OnboardingProfileResponse> getOnboardingProfile(
            @CurrentMember final Long memberId
    ) {
        return ResponseEntity.ok(onboardingService.getOnboardingInfo(memberId));
    }

    @Override
    @GetMapping("/profile/purposes")
    @RoleAuth(role = {Role.USER, Role.PENDING})
    public ResponseEntity<SuccessResponse<List<OnboardingOptionResponse>>> getOnboardingPurposes() {
        List<OnboardingOptionResponse> response = List.of(OnboardingPurpose.values()).stream()
                .map(OnboardingOptionResponse::from)
                .toList();

        return ResponseEntity.ok()
                .body(SuccessResponse.of(MemberSuccessCode.ONBOARDING_PURPOSE_LIST_SUCCESS, response));
    }

    @Override
    @GetMapping("/profile/habits")
    @RoleAuth(role = {Role.USER, Role.PENDING})
    public ResponseEntity<SuccessResponse<List<OnboardingOptionResponse>>> getOnboardingHabits() {
        List<OnboardingOptionResponse> response = List.of(OnboardingHabit.values()).stream()
                .map(OnboardingOptionResponse::from)
                .toList();

        return ResponseEntity.ok()
                .body(SuccessResponse.of(MemberSuccessCode.ONBOARDING_HABIT_LIST_SUCCESS, response));
    }

    @Override
    @PatchMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RoleAuth(role = {Role.USER, Role.PENDING})
    public ResponseEntity<SuccessResponse<Void>> updateProfile(
            @CurrentMember Long memberId,
            @Valid @RequestPart(name = "request") ProfileSettingRequest request,
            @RequestPart(name = "image", required = false) MultipartFile profileImage
    ) {
        onboardingService.updateProfile(memberId, request, profileImage);
        onboardingService.assignMealStyle(memberId, request.habits());
        return ResponseEntity.ok().body(SuccessResponse.from(MemberSuccessCode.PROFILE_SETTING_SUCCESS));
    }

    @Override
    @DeleteMapping
    @RoleUserAuth
    public ResponseEntity<SuccessResponse<Void>> quitMember(
            @CurrentMember Long memberId){
        memberService.quit(memberId);
        return ResponseEntity.ok()
                .body(SuccessResponse.from(MemberSuccessCode.USER_DELETE_SUCCESS));
    }

    @Override
    @RoleUserAuth
    public ResponseEntity<SuccessResponse<MyPageResponse>> myPage(Long memberId) {
        MyPageResponse myPageResponse = myPageService.myPage(memberId);
        return ResponseEntity.ok()
                .body(SuccessResponse.of(MemberSuccessCode.MYPAGE_GET_SUCCESS, myPageResponse));
    }


}
