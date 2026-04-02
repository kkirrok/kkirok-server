package com.kkirok.server.domain.member.api;

import com.kkirok.server.domain.member.application.dto.request.FindEmailRequest;
import com.kkirok.server.domain.member.application.dto.request.ProfileSettingRequest;
import com.kkirok.server.domain.member.application.dto.request.ResetPasswordRequest;
import com.kkirok.server.domain.member.application.dto.response.FoundEmailResponse;
import com.kkirok.server.domain.member.application.dto.response.OnboardingProfileResponse;
import com.kkirok.server.domain.member.application.service.AccountRecoveryService;
import com.kkirok.server.domain.member.application.service.MemberService;
import com.kkirok.server.domain.member.application.service.OnboardingService;
import com.kkirok.server.domain.member.exception.MemberSuccessCode;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.auth.annotation.RoleUserAuth;
import com.kkirok.server.global.common.dto.SuccessResponse;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class MemberController implements MemberApi {

    private final AccountRecoveryService accountRecoveryService;
    private final OnboardingService onboardingService;
    private final MemberService memberService;

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
            @Parameter(hidden = true) @CurrentMember final Long memberId,
            @Valid @RequestBody final ResetPasswordRequest request
    ) {
        accountRecoveryService.resetPassword(memberId, request);
        return ResponseEntity.ok().body(SuccessResponse.from(MemberSuccessCode.RESET_PASSWORD_SUCCESS));
    }

    @Override
    @GetMapping("/profile")
    @RoleUserAuth
    public ResponseEntity<OnboardingProfileResponse> getOnboardingProfile(
            @Parameter(hidden = true) @CurrentMember final Long memberId
    ) {
        return ResponseEntity.ok(onboardingService.getOnboardingInfo(memberId));
    }

    @Override
    @PostMapping("/profile")
    @RoleUserAuth
    public ResponseEntity<SuccessResponse<Void>> updateProfile(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @ModelAttribute @Valid ProfileSettingRequest request,
            @RequestPart(required = false) MultipartFile profileImage
    ) {
        onboardingService.updateProfile(memberId, request, profileImage);
        return ResponseEntity.ok().body(SuccessResponse.from(MemberSuccessCode.PROFILE_SETTING_SUCCESS));
    }

    @Override
    @DeleteMapping
    @RoleUserAuth
    public ResponseEntity<SuccessResponse<Void>> quitMember( @Parameter(hidden = true) @CurrentMember Long memberId){
        memberService.quit(memberId);
        return ResponseEntity.ok()
                .body(SuccessResponse.from(MemberSuccessCode.USER_DELETE_SUCCESS));
    }


}
