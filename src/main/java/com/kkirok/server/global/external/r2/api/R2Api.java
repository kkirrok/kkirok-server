package com.kkirok.server.global.external.r2.api;

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
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "R2 API", description = "Cloudflare R2 파일 관련 API")
public interface R2Api {

    @Operation(
            summary = "다운로드 Presigned URL 발급",
            description = """
                R2 객체 키로 다운로드용 Presigned URL을 발급합니다.
                
                끼록 내의 api에서 파일( 이미지, pdf 등 )은 key 형태로 반환됩니다.
                key를 해당 api에 넣어 요청하면 presigned url를 받을 수 있습니다.
                각 Url은 5분의 유효 기간이 존재합니다.
                
                - 요청 파라미터: `key` -> UUID + Keyword 조합으로 이루어져 있습니다.
                - 응답: key + downloadUrl
            """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(codeType = ExternalErrorCode.class, code = "R2_INVALID_OBJECT_KEY"),
            @ApiErrorCodeExample(codeType = ExternalErrorCode.class, code = "R2_PRESIGNED_URL_GENERATION_FAILED")
    })
    @ApiSuccessCodeExample(codeType = ExternalSuccessCode.class, code = "R2_DOWNLOAD_PRESIGNED_URL_SUCCESS")
    ResponseEntity<SuccessResponse<PresignedResponse>> download(
            @Parameter(description = "다운로드할 R2 객체 키", required = true)
            @PathVariable String key
    );

}
