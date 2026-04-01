package com.kkirok.server.global.external.openai.prompt;

import com.kkirok.server.global.external.exception.ExternalErrorCode;
import com.kkirok.server.global.external.exception.PromptException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/*
	프롬프트가 담긴 txt 파일을 읽어오는 Service
	매번 file I/O가 발생하지 않도록 ConcurrentHashMap에 캐싱
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromptService {

	private static final String CLASSPATH_PREFIX = "classpath:";

	private final ResourceLoader resourceLoader;
	private final ConcurrentMap<PromptType, String> promptCache = new ConcurrentHashMap<>();

	// getPrompt
	public PromptTemplate getPrompt(PromptType promptType) {
		return new PromptTemplate(loadPrompt(promptType), promptType.getVersion());
	}

	public PromptTemplate getPrompt(PromptType promptType, Map<String, ?> variables) {
		return new PromptTemplate(render(loadPrompt(promptType), variables), promptType.getVersion());
	}

	// loadPrompt
	private String loadPrompt(PromptType promptType) {
		return promptCache.computeIfAbsent(promptType, this::readPrompt);
	}

	// readPrompt
	private String readPrompt(PromptType promptType) {
		Resource resource = resourceLoader.getResource(CLASSPATH_PREFIX + promptType.getResourcePath());
		try (InputStream inputStream = resource.getInputStream()) {
			return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException exception) {
			log.error("Failed to load prompt file. path={}", promptType.getResourcePath(), exception);
			throw new PromptException(ExternalErrorCode.OPENAI_PROMPT_LOAD_FAILED, exception);
		}
	}


	private String render(String template, Map<String, ?> variables) {
		if (variables == null || variables.isEmpty()) {
			return template;
		}

		String rendered = template;
		for (Map.Entry<String, ?> entry : variables.entrySet()) {
			String placeholder = "{{" + entry.getKey() + "}}";
			Object value = entry.getValue();
			rendered = rendered.replace(placeholder, value == null ? "" : String.valueOf(value));
		}
		return rendered;
	}
}
