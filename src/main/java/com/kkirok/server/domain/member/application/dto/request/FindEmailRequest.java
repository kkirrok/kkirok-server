package com.kkirok.server.domain.member.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record FindEmailRequest(
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 20, message = "이름은 20자 이하여야 합니다.")
        @Schema(example = "김준용", description = "사용자 이름")
        String name,

        @NotNull(message = "생년월일은 필수입니다.")
        @Past(message = "생년월일은 과거 날짜여야 합니다.")
        @Schema(example = "2002-04-13", description = "생년월일")
        LocalDate birth,

        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^01[0-9]{8,9}$", message = "전화번호 형식이 올바르지 않습니다.")
        @Schema(example = "01012345678", description = "전화번호")
        String phone
) {
}
