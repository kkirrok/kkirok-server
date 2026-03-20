package com.kkirok.server.domain.character.application.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

// 캐릭터 기본 정보
public record CharacterDefaultInfoResponse(
        @Schema(example = "1")
        Long characterId,
        @Schema(example = "4")
        Integer level,
        @Schema(example = "뀨뀨뀨")
        String name,
        @Schema(example = "120")
        Integer curExp,
        @Schema(example = "150")
        Integer maxExp,
        @Schema(example = "550e8400-e29b-41d4-a716-446655440000-file")
        String image,
        @Schema(example = "1200")
        Integer kcalCur,
        @Schema(example = "1500")
        Integer kcalMax,
        @Schema(example = "50")
        Integer carbohydrateG,
        @Schema(example = "80")
        Integer proteinG,
        @Schema(example = "30")
        Integer fatG,
        @Schema(example = "30")
        Integer sugarG,
        @Schema(example = "30")
        Integer sodiumG
) {
}
