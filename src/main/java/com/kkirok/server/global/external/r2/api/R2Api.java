package com.kkirok.server.global.external.r2.api;

import com.kkirok.server.domain.terms.exception.TermsErrorCode;
import com.kkirok.server.global.common.dto.SuccessResponse;
import com.kkirok.server.global.external.exception.ExternalErrorCode;
import com.kkirok.server.global.external.exception.ExternalSuccessCode;
import com.kkirok.server.global.external.r2.application.dto.response.PresignedResponse;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExample;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExamples;
import com.kkirok.server.global.swagger.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "R2 API", description = "Cloudflare R2 파일 관련 API")
public interface R2Api {

    @Operation(
            summary = "다운로드 Presigned URL 발급 [USER, ADMIN]",
            description = """
                R2 객체 키로 다운로드용 Presigned URL을 발급합니다.
                
                끼록 내의 api에서 파일( 이미지, pdf 등 )은 key 형태로 반환됩니다.
                key를 해당 api에 쿼리 파라미터로 넣어 요청하면 presigned url를 받을 수 있습니다.
                cloudflare.r2.public-base-url가 설정되어 있으면 CDN/public URL을 반환합니다.
                각 Url은 5분의 유효 기간이 존재합니다.
                
                - 요청 파라미터: `key` -> 도메인 prefix + UUID + 확장자 조합으로 이루어져 있으며 슬래시(`/`)를 포함할 수 있습니다. (예: `profile/uuid.jpg`)
                - 응답: key + downloadUrl
            """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = ExternalErrorCode.class, code = "R2_INVALID_OBJECT_KEY"),
            @ApiErrorCodeExample(codeType = ExternalErrorCode.class, code = "R2_PRESIGNED_URL_GENERATION_FAILED"),
            @ApiErrorCodeExample(codeType = TermsErrorCode.class, code = "TERMS_AGREEMENT_REQUIRED")
    })
    @ApiSuccessCodeExample(codeType = ExternalSuccessCode.class, code = "R2_DOWNLOAD_PRESIGNED_URL_SUCCESS")
    ResponseEntity<SuccessResponse<PresignedResponse>> download(
            @Parameter(description = "다운로드할 R2 객체 키 (슬래시 포함 가능)", required = true)
            @RequestParam String key
    );

}
