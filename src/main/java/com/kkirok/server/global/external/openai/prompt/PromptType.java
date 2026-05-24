package com.kkirok.server.global.external.openai.prompt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PromptType {

	MEAL_ANALYSIS("prompts/meal-analysis-v1.txt", "v1"),
	MEAL_RECOMMENDATION("prompts/meal-recommendation-v1.txt", "v1"),
	MEAL_WEEKLY_REPORT("prompts/meal-weekly-report-v1.txt", "v1"),
	MEAL_STYLE_CLASSIFICATION("prompts/meal-style-classification-v1.txt", "v1"),
	KKINIPOP_MISSION("prompts/kkinipop-mission-v1.txt", "v1");

	private final String resourcePath;
	private final String version;
}