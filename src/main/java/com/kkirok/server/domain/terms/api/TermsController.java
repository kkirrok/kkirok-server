package com.kkirok.server.domain.terms.api;

import com.kkirok.server.domain.terms.application.dto.request.TermsAgreeRequest;
import com.kkirok.server.domain.terms.application.dto.response.TermsResponse;
import com.kkirok.server.domain.terms.application.service.TermsService;
import com.kkirok.server.domain.terms.exception.TermsSuccessCode;
import com.kkirok.server.domain.user.domain.Role;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.auth.annotation.RoleAuth;
import com.kkirok.server.global.auth.annotation.TermsCheckExempt;
import com.kkirok.server.global.common.dto.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/terms")
@RequiredArgsConstructor
@TermsCheckExempt
public class TermsController implements TermsApi {

    private final TermsService termsService;

    @Override
    @GetMapping
    public ResponseEntity<SuccessResponse<List<TermsResponse>>> getAllLatestTerms() {
        return ResponseEntity.ok()
                .body(SuccessResponse.of(TermsSuccessCode.TERMS_LIST_SUCCESS, termsService.getAllLatestTerms()));
    }

    @Override
    @PostMapping("/agree")
    @RoleAuth(role = {Role.USER, Role.PENDING})
    public ResponseEntity<SuccessResponse<Void>> agree(
            @CurrentMember Long memberId,
            @Valid @RequestBody TermsAgreeRequest request
    ) {
        termsService.agree(memberId, request);
        return ResponseEntity.ok().body(SuccessResponse.from(TermsSuccessCode.TERMS_AGREE_SUCCESS));
    }
}
