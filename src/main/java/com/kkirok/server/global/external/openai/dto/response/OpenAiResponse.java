package com.kkirok.server.global.external.openai.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

// /v1/responses 응답 body를 매핑하는 최상위 DTO
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiResponse(
		String id,
		String model,
		@JsonAlias("request_id")
		String requestId,
		List<OpenAiOutputItem> output,
		OpenAiUsage usage
) {

	// output 배열 전체를 순회하면서 첫 번째 텍스트 응답을 찾는다
	@JsonIgnore
	public String getFirstOutputText() {
		if (output == null || output.isEmpty()) {
			return null;
		}
		for (OpenAiOutputItem item : output) {
			String text = item.getFirstText();
			if (text != null) {
				return text;
			}
		}
		return null;
	}
}
