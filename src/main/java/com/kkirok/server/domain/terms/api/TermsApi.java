package com.kkirok.server.domain.terms.api;

import com.kkirok.server.domain.terms.application.dto.request.TermsAgreeRequest;
import com.kkirok.server.domain.terms.application.dto.response.TermsResponse;
import com.kkirok.server.domain.terms.exception.TermsErrorCode;
import com.kkirok.server.domain.terms.exception.TermsSuccessCode;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.common.dto.SuccessResponse;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExample;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExamples;
import com.kkirok.server.global.swagger.annotation.ApiSuccessCodeExample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Terms API", description = "약관 관련 API")
public interface TermsApi {

    @Operation(
            summary = "최신 약관 목록 조회 []",
            description = """
                    약관 유형(`type`)별로 가장 최신 버전 하나씩만 조회합니다. 인증 없이 호출할 수 있는 공개 API입니다.

                    - 약관 본문은 R2에 마크다운/텍스트 파일로 저장되어 있고, `url`은 그 파일을 바로 내려받을 수 있는 다운로드 URL입니다.
                      (R2 공개 base URL이 설정되어 있으면 고정 CDN URL, 아니면 5분 TTL의 presigned URL이 매 호출마다 새로 발급됩니다.)
                    - `is_required`가 `true`인 항목은 필수 약관(이용약관, 개인정보 수집·이용, 개인정보 제3자 제공), `false`는 선택 약관(마케팅 정보 수신 동의)입니다.
                    - 회원가입/온보딩 화면에서 전체 약관 목록과 본문을 보여줄 때 사용합니다.
                    """
    )
    @ApiSuccessCodeExample(codeType = TermsSuccessCode.class, code = "TERMS_LIST_SUCCESS")
    ResponseEntity<SuccessResponse<List<TermsResponse>>> getAllLatestTerms();

    @Operation(
            summary = "약관 동의 [USER, PENDING]",
            description = """
                    약관 유형별 최신 버전에 동의(또는 거부)합니다. 버전 번호는 클라이언트가 신경 쓰지 않아도 되며, 서버가 각 `type`의 최신 버전을 찾아 동의 이력을 기록합니다.

                    - 필수 약관(`is_required = true`)은 `is_agree`를 `false`로 보내면 요청 전체가 실패합니다. 필수 약관은 반드시 동의해야만 서비스를 이용할 수 있습니다.
                    - 선택 약관(마케팅 정보 수신 동의)은 `is_agree`를 `true`/`false` 어느 쪽으로 보내도 됩니다.
                    - 이미 동의/거부 이력이 있는 유형에 다시 요청하면 기존 이력이 최신 값으로 갱신됩니다(덮어쓰기).
                    - 회원가입 직후(PENDING 권한) 로그인 응답의 `pending_terms_agree`에 포함된 약관, 또는 약관이 개정되어 재동의가 필요한 약관 모두 이 API로 처리합니다.

                    ## 요청 예시
                    ```json
                    {
                      "agrees": [
                        { "type": "TERMS_OF_SERVICE", "is_agree": true },
                        { "type": "PRIVACY_COLLECTION", "is_agree": true },
                        { "type": "PRIVACY_THIRD_PARTY", "is_agree": true },
                        { "type": "MARKETING", "is_agree": false }
                      ]
                    }
                    ```
                    """
    )
    @ApiErrorCodeExamples({
            @ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
            @ApiErrorCodeExample(codeType = TermsErrorCode.class, code = "REQUIRED_TERMS_NOT_AGREED"),
            @ApiErrorCodeExample(codeType = TermsErrorCode.class, code = "TERMS_TYPE_DUPLICATE"),
            @ApiErrorCodeExample(codeType = TermsErrorCode.class, code = "TERMS_NOT_FOUND")
    })
    @ApiSuccessCodeExample(codeType = TermsSuccessCode.class, code = "TERMS_AGREE_SUCCESS")
    ResponseEntity<SuccessResponse<Void>> agree(
            @Parameter(hidden = true) @CurrentMember Long memberId,
            @Valid @RequestBody TermsAgreeRequest request
    );
}
