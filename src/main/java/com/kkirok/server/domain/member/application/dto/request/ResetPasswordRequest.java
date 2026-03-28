package com.kkirok.server.domain.member.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 아닙니다.")
        @Schema(example = "email@naver.com", description = "계정 이메일")
        String email,

        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 20, message = "이름은 20자 이하여야 합니다.")
        @Schema(example = "김준용", description = "사용자 이름")
        String name,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다.")
        @Schema(example = "newPassword123!", description = "새 비밀번호")
        String newPassword
) {
}
