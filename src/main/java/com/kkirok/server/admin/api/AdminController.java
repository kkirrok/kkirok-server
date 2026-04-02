package com.kkirok.server.admin.api;

import com.kkirok.server.admin.application.dto.request.AdminAccountRequestCreateRequest;
import com.kkirok.server.admin.application.dto.response.AdminAccountRequestResponse;
import com.kkirok.server.admin.application.dto.response.AdminApprovalResponse;
import com.kkirok.server.admin.application.service.AdminAccountRequestService;
import com.kkirok.server.admin.domain.AdminRequestStatus;
import com.kkirok.server.admin.exception.AdminSuccessCode;
import com.kkirok.server.global.auth.annotation.CurrentMember;
import com.kkirok.server.global.auth.annotation.RoleAdminAuth;
import com.kkirok.server.global.common.dto.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminController implements AdminApi {

	private final AdminAccountRequestService adminAccountRequestService;

	@PostMapping("/account-requests")
	public ResponseEntity<SuccessResponse<AdminAccountRequestResponse>> createAdminAccountRequest(
			@Valid @RequestBody AdminAccountRequestCreateRequest request
	) {
		AdminAccountRequestResponse response = adminAccountRequestService.createRequest(request);
		return ResponseEntity.status(AdminSuccessCode.ADMIN_ACCOUNT_REQUEST_CREATED.getStatus())
				.body(SuccessResponse.of(AdminSuccessCode.ADMIN_ACCOUNT_REQUEST_CREATED, response));
	}

	@GetMapping("/account-requests")
	@RoleAdminAuth
	public ResponseEntity<SuccessResponse<List<AdminAccountRequestResponse>>> getAdminAccountRequests(
			@RequestParam(defaultValue = "PENDING") AdminRequestStatus status
	) {
		return ResponseEntity.ok(
				SuccessResponse.of(AdminSuccessCode.ADMIN_ACCOUNT_REQUEST_LIST_SUCCESS, adminAccountRequestService.getRequests(status))
		);
	}

	@PatchMapping("/account-requests/{requestId}/status")
	@RoleAdminAuth
	public ResponseEntity<SuccessResponse<AdminApprovalResponse>> processAdminAccountRequest(
			@CurrentMember Long memberId,
			@PathVariable Long requestId,
			@RequestParam(defaultValue = "APPROVED") AdminRequestStatus status
	) {
		AdminApprovalResponse response = adminAccountRequestService.process(requestId, memberId, status);
		return ResponseEntity.ok(SuccessResponse.of(AdminSuccessCode.ADMIN_ACCOUNT_REQUEST_STATUS_UPDATED, response));
	}
}
