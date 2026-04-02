package com.kkirok.server.admin.application.dto.response;

import com.kkirok.server.admin.domain.AdminAccountRequest;
import com.kkirok.server.admin.domain.AdminRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

// 관리자 요청 응답
public record AdminAccountRequestResponse(
		@Schema(description = "관리자 계정 신청 ID", example = "1")
		Long requestId,
		@Schema(description = "신청 이메일", example = "example@kkirok.com")
		String email,
		@Schema(description = "신청자 이름", example = "김끼록")
		String name,
		@Schema(description = "관리자 권한 요청 사유", example = "운영 이슈 대응 및 관리자 페이지 접근이 필요합니다.")
		String reason,
		@Schema(description = "요청 상태", example = "PENDING")
		AdminRequestStatus status,
		@Schema(description = "관리자 계정 신청 생성 시각", example = "2026-04-02T15:30:00")
		LocalDateTime createdAt
) {
	public static AdminAccountRequestResponse from(AdminAccountRequest request) {
		return new AdminAccountRequestResponse(
				request.getId(),
				request.getEmail(),
				request.getName(),
				request.getReason(),
				request.getStatus(),
				request.getCreatedAt()
		);
	}
}
