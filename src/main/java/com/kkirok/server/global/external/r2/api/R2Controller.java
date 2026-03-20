package com.kkirok.server.global.external.r2.api;

import com.kkirok.server.global.common.dto.SuccessResponse;
import com.kkirok.server.global.external.r2.application.dto.response.PresignedResponse;
import com.kkirok.server.global.external.r2.application.service.PresignedUrlService;
import com.kkirok.server.global.external.r2.exception.R2SuccessCode;
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
public class R2Controller implements R2Api {

    private final PresignedUrlService presignedUrlService;

    @Override
    @GetMapping("/{key}/download")
    public ResponseEntity<SuccessResponse<PresignedResponse>> download(@PathVariable String key) {
        URL result = presignedUrlService.getPresignedUrl(key);
        PresignedResponse response = PresignedResponse.from(key, result.toString());

        return ResponseEntity.ok()
            .body(SuccessResponse.of(R2SuccessCode.DOWNLOAD_PRESIGNED_URL_SUCCESS, response));
    }
}
