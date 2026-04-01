package com.kkirok.server.global.external.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

// OpenAI output 배열의 각 항목을 표현하는 응답 DTO
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiOutputItem(
		String type,
		List<OpenAiOutputContent> content
) {

	// 항목 안에서 첫 번째 비어 있지 않은 텍스트를 꺼낸다
	public String getFirstText() {
		if (content == null || content.isEmpty()) {
			return null;
		}
		for (OpenAiOutputContent part : content) {
			String text = part.text();
			if (text != null && !text.isBlank()) {
				return text;
			}
		}
		return null;
	}
}
