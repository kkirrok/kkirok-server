package com.kkirok.server.domain.character.application.dto.res;

import com.kkirok.server.domain.character.domain.Character;
import com.kkirok.server.domain.character.util.CharacterUtilLevel;
import com.kkirok.server.domain.meal.domain.MealAiAnalysis;
import com.kkirok.server.domain.meal.domain.MealRecord;
import com.kkirok.server.domain.meal.util.NutrientAvgUtil;
import com.kkirok.server.domain.meal.util.NutrientMaxUtil;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

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

    public static CharacterDefaultInfoResponse create(Character character, List<MealRecord> mealRecord) {
        return new CharacterDefaultInfoResponse(
                character.getId(),
                character.getLevel(),
                character.getName(),
                character.getExp(),
                CharacterUtilLevel.getMaximumLevel(character.getExp()),
//                character.getCharacterType().getBaseImage(),
                null, // TODO: 이미지 넣기
                NutrientAvgUtil.getAvg(mealRecord, analysis -> analysis.getKcal().longValue()),
                NutrientMaxUtil.getMax(mealRecord, analysis -> analysis.getKcal().longValue()),
                NutrientAvgUtil.getAvg(mealRecord, MealAiAnalysis::getCarbohydrateG),
                NutrientAvgUtil.getAvg(mealRecord, MealAiAnalysis::getProteinG),
                NutrientAvgUtil.getAvg(mealRecord, MealAiAnalysis::getFatG),
                NutrientAvgUtil.getAvg(mealRecord, MealAiAnalysis::getSugarG),
                NutrientAvgUtil.getAvg(mealRecord, MealAiAnalysis::getSodiumMg)
        );
    }

}
