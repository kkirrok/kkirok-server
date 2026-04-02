package com.kkirok.server.admin.api;

import com.kkirok.server.admin.application.dto.request.AdminAccountRequestCreateRequest;
import com.kkirok.server.admin.application.dto.response.AdminAccountRequestResponse;
import com.kkirok.server.admin.application.dto.response.AdminApprovalResponse;
import com.kkirok.server.admin.domain.AdminRequestStatus;
import com.kkirok.server.admin.exception.AdminErrorCode;
import com.kkirok.server.admin.exception.AdminSuccessCode;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Admin API", description = "관리자 계정 신청 및 승인 관련 API")
public interface AdminApi {

	@Operation(
			summary = "관리자 계정 신청",
			description = """
					관리자 계정 생성을 요청합니다.

					- 별도 권한 없이 호출할 수 있습니다.
					- 이메일은 반드시 `@kkirok.com` 도메인이어야 합니다.
					- 아직 실제 관리자 계정은 생성되지 않고, 승인 대기 요청만 저장됩니다.
					"""
	)
	@ApiErrorCodeExamples({
			@ApiErrorCodeExample(codeType = AdminErrorCode.class, code = "ADMIN_EMAIL_DOMAIN_INVALID"),
			@ApiErrorCodeExample(codeType = AdminErrorCode.class, code = "ADMIN_ACCOUNT_REQUEST_ALREADY_EXISTS"),
			@ApiErrorCodeExample(codeType = AdminErrorCode.class, code = "ADMIN_ACCOUNT_EMAIL_ALREADY_EXISTS")
	})
	@ApiSuccessCodeExample(codeType = AdminSuccessCode.class, code = "ADMIN_ACCOUNT_REQUEST_CREATED")
	ResponseEntity<SuccessResponse<AdminAccountRequestResponse>> createAdminAccountRequest(
			@Valid @RequestBody AdminAccountRequestCreateRequest request
	);

	@Operation(
			summary = "관리자 계정 신청 목록 조회",
			description = """
					승인 대기 중이거나 이미 처리된 관리자 계정 신청 목록을 조회합니다.

					- 관리자 권한이 필요합니다.
					- `status` 필터를 지원합니다.
					- 기본값은 `PENDING`입니다.
					- 최신 요청 순으로 반환됩니다.
					"""
	)
	@ApiErrorCodeExamples({
			@ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
			@ApiErrorCodeExample(status = 403, message = "관리자 권한이 필요합니다.", exampleName = "FORBIDDEN")
	})
	@ApiSuccessCodeExample(codeType = AdminSuccessCode.class, code = "ADMIN_ACCOUNT_REQUEST_LIST_SUCCESS")
	ResponseEntity<SuccessResponse<List<AdminAccountRequestResponse>>> getAdminAccountRequests(
			@Parameter(description = "조회할 관리자 요청 상태. 기본값은 PENDING입니다.", required = false)
			@RequestParam(defaultValue = "PENDING") AdminRequestStatus status
	);

	@Operation(
			summary = "관리자 계정 신청 상태 처리",
			description = """
					관리자 계정 신청 상태를 처리합니다.

					- 관리자 권한이 필요합니다.
					- `status` 기본값은 `APPROVED`입니다.
					- `APPROVED` 선택 시 실제 `ROLE_ADMIN` 권한의 로컬 계정이 생성됩니다.
					- `REJECTED` 선택 시 요청만 반려 처리됩니다.
					- `PENDING`은 처리 상태로 사용할 수 없습니다.
					- 이미 처리된 요청은 다시 변경할 수 없습니다.
					"""
	)
	@ApiErrorCodeExamples({
			@ApiErrorCodeExample(status = 401, message = "인증이 필요합니다.", exampleName = "UNAUTHORIZED"),
			@ApiErrorCodeExample(status = 403, message = "관리자 권한이 필요합니다.", exampleName = "FORBIDDEN"),
			@ApiErrorCodeExample(codeType = AdminErrorCode.class, code = "ADMIN_ACCOUNT_REQUEST_STATUS_INVALID"),
			@ApiErrorCodeExample(codeType = AdminErrorCode.class, code = "ADMIN_ACCOUNT_REQUEST_NOT_FOUND"),
			@ApiErrorCodeExample(codeType = AdminErrorCode.class, code = "ADMIN_ACCOUNT_REQUEST_NOT_PENDING"),
			@ApiErrorCodeExample(codeType = AdminErrorCode.class, code = "ADMIN_ACCOUNT_EMAIL_ALREADY_EXISTS")
	})
	@ApiSuccessCodeExample(codeType = AdminSuccessCode.class, code = "ADMIN_ACCOUNT_REQUEST_STATUS_UPDATED")
	ResponseEntity<SuccessResponse<AdminApprovalResponse>> processAdminAccountRequest(
			@CurrentMember Long memberId,
			@Parameter(description = "처리할 관리자 계정 요청 ID", required = true)
			@PathVariable Long requestId,
			@Parameter(description = "변경할 관리자 요청 상태. 기본값은 APPROVED입니다.", required = false)
			@RequestParam(defaultValue = "APPROVED") AdminRequestStatus status
	);
}
