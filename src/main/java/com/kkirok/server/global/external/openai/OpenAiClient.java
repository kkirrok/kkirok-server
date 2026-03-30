package com.kkirok.server.global.external.openai;


import com.kkirok.server.global.external.openai.dto.request.OpenAiResponseRequest;
import com.kkirok.server.global.external.openai.dto.response.OpenAiResponse;

public interface OpenAiClient {

	OpenAiResponse createResponse(OpenAiResponseRequest request);

}
