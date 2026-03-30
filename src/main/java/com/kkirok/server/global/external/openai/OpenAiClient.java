package com.kkirok.server.global.external.openai;


import com.kkirok.server.global.external.ExternalClient;
import com.kkirok.server.global.external.openai.dto.request.OpenAiResponseRequest;
import com.kkirok.server.global.external.openai.dto.response.OpenAiResponse;

public interface OpenAiClient extends ExternalClient<OpenAiResponseRequest, OpenAiResponse> {

	OpenAiResponse createResponse(OpenAiResponseRequest request);

	@Override
	default OpenAiResponse execute(OpenAiResponseRequest request) {
		return createResponse(request);
	}

}
