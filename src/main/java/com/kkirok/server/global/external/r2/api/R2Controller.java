package com.kkirok.server.global.external.r2.api;

import com.kkirok.server.domain.user.domain.Role;
import com.kkirok.server.global.auth.annotation.RoleAuth;
import com.kkirok.server.global.common.dto.SuccessResponse;
import com.kkirok.server.global.external.exception.ExternalSuccessCode;
import com.kkirok.server.global.external.r2.application.dto.response.PresignedResponse;
import com.kkirok.server.global.external.r2.application.service.PresignedUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;

@RestController
@RequestMapping("/v1/r2")
@RequiredArgsConstructor
@RoleAuth(role = {Role.USER, Role.ADMIN})
public class R2Controller implements R2Api {

    private final PresignedUrlService presignedUrlService;

    @Override
    @GetMapping("/{key}/download")
    public ResponseEntity<SuccessResponse<PresignedResponse>> download(@PathVariable String key) {
        URL result = presignedUrlService.getPresignedUrl(key);
        PresignedResponse response = PresignedResponse.from(key, result.toString());

        return ResponseEntity.ok()
            .body(SuccessResponse.of(ExternalSuccessCode.R2_DOWNLOAD_PRESIGNED_URL_SUCCESS, response));
    }
}
