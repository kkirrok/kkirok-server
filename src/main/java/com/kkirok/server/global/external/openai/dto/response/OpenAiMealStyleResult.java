package com.kkirok.server.global.external.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenAiMealStyleResult(
        @JsonProperty("mealStyle") String mealStyle
) {
}
