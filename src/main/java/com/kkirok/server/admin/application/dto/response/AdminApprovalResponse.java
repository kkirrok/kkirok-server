package com.kkirok.server.admin.application.dto.response;

import com.kkirok.server.admin.domain.AdminRequestStatus;
import com.kkirok.server.domain.user.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;

// 관리자 승인 응답
public record AdminApprovalResponse(
		@Schema(description = "처리된 관리자 계정 신청 ID", example = "1")
		Long requestId,
		@Schema(description = "처리 후 관리자 계정 요청 상태", example = "APPROVED")
		AdminRequestStatus status,
		@Schema(description = "생성된 관리자 member ID, REJECTED인 경우 null", example = "42", nullable = true)
		Long memberId,
		@Schema(description = "처리된 관리자 이메일", example = "owner@kkirok.com")
		String email,
		@Schema(description = "부여된 시스템 역할, REJECTED인 경우 null", example = "ADMIN", nullable = true)
		Role role
) {
	public static AdminApprovalResponse of(
			Long requestId,
			AdminRequestStatus status,
			Long memberId,
			String email,
			Role role
	) {
		return new AdminApprovalResponse(requestId, status, memberId, email, role);
	}
}
