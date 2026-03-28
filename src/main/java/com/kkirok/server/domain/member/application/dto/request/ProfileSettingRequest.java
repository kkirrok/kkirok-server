package com.kkirok.server.domain.member.application.dto.request;

import com.kkirok.server.domain.member.domain.Gender;
import com.kkirok.server.domain.member.domain.OnboardingHabit;
import com.kkirok.server.domain.member.domain.OnboardingPurpose;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record ProfileSettingRequest(

        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 20, message = "이름은 20자 이하여야 합니다.")
        @Schema(example = "김준용", description = "사용자 이름")
        String name,

        @Past(message = "생년월일은 과거 날짜여야 합니다.")
        @Schema(example = "2002-04-13", description = "생년월일")
        LocalDate birth,

        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^01[0-9]{8,9}$", message = "전화번호 형식이 올바르지 않습니다.")
        @Schema(example = "01012345678", description = "전화번호")
        String phone,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 8, message = "닉네임은 2자 이상 8자 이하이어야 합니다.")
        @Schema(example = "멋진닉네임", description = "닉네임")
        String nickname,

        @NotNull(message = "성별은 필수입니다.")
        @Schema(example = "MALE", description = "성별")
        Gender gender,

        @Schema(example = "LOSE_WEIGHT", description = "온보딩 목표", nullable = true)
        OnboardingPurpose purpose,

        @Size(max = 5, message = "식습관은 최대 5개까지 선택할 수 있습니다.")
        @ArraySchema(
                arraySchema = @Schema(description = "식습관 목록"),
                schema = @Schema(example = "MEAT")
        )
        List<OnboardingHabit> habits

) {
}
