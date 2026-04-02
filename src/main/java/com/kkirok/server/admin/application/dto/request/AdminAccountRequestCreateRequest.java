package com.kkirok.server.admin.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 관리자 계정 신청 요청
public record AdminAccountRequestCreateRequest(

		@Schema(description = "이메일", example = "example@kkirok.com")
		@Email(message = "올바른 이메일 형식이어야 합니다.")
		@NotBlank(message = "email은 비어 있을 수 없습니다.")
		String email,

		@Schema(description = "비밀번호", example = "adminPassword123!")
		@NotBlank(message = "password는 비어 있을 수 없습니다.")
		@Size(min = 8, message = "password는 8자 이상이어야 합니다.")
		String password,

		@Schema(description = "관리자 신청자 실명", example = "김끼록")
		@NotBlank(message = "name은 비어 있을 수 없습니다.")
		String name,

		@Schema(description = "관리자 권한이 필요한 이유", example = "운영 이슈 대응 및 관리자 페이지 접근이 필요합니다.")
		@NotBlank(message = "reason은 비어 있을 수 없습니다.")
		String reason
) {
}
