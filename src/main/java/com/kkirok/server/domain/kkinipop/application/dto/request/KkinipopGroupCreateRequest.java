package com.kkirok.server.domain.kkinipop.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "끼니팝 그룹 생성 요청")
public record KkinipopGroupCreateRequest(
        @Schema(description = "그룹 이름", example = "아침 챌린저스")
        @NotBlank(message = "그룹 이름은 필수입니다.")
        @Size(max = 40, message = "그룹 이름은 40자 이하여야 합니다.")
        String name
) {
}
