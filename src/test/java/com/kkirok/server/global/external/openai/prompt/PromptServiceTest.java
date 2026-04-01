package com.kkirok.server.global.external.openai.prompt;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PromptServiceTest {

	private final PromptService promptService = new PromptService(new DefaultResourceLoader());

	@Test
	void promptFileCanBeLoaded() {
		PromptTemplate prompt = promptService.getPrompt(PromptType.MEAL_ANALYSIS);

		assertThat(prompt.version()).isEqualTo("v1");
		assertThat(prompt.content()).contains("nutrition assistant");
	}

	@Test
	void promptVariablesCanBeRendered() {
		PromptTemplate prompt = promptService.getPrompt(
				PromptType.MEAL_ANALYSIS,
				Map.of("meal_context", "Lunch: chicken salad")
		);

		assertThat(prompt.content()).contains("Lunch: chicken salad");
		assertThat(prompt.content()).doesNotContain("{{meal_context}}");
	}
}
