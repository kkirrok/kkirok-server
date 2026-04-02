package com.kkirok.server.domain.member.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LocalLoginRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 아닙니다.")
        @Schema(description = "로그인에 사용할 이메일", example = "user@kkirok.com")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다.")
        @Schema(description = "로그인 비밀번호", example = "password123!")
        String password
) {
}
