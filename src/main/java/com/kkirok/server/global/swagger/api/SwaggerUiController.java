package com.kkirok.server.global.swagger.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ClassPathResource;
import org.springdoc.webmvc.api.MultipleOpenApiWebMvcResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

@Hidden
@Controller
public class SwaggerUiController {

	private static final String API_DOCS_BASE_PATH = "/api-specs";

	private final MultipleOpenApiWebMvcResource multipleOpenApiResource;

	public SwaggerUiController(MultipleOpenApiWebMvcResource multipleOpenApiResource) {
		this.multipleOpenApiResource = multipleOpenApiResource;
	}

	@GetMapping("/api-docs")
	public void blockApiDocsRoot(HttpServletResponse response) throws IOException {
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}

	@GetMapping(value = "/api-docs/openapi", produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String generalSwaggerUi(HttpServletRequest request) {
		return loadSwaggerUiHtml(request, "Kkirok OpenAPI Docs", "general");
	}

	@GetMapping(value = "/api-docs/admin", produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String adminSwaggerUi(HttpServletRequest request) {
		return loadSwaggerUiHtml(request, "Kkirok Admin API Docs", "admin");
	}

	private String loadSwaggerUiHtml(HttpServletRequest request, String title, String group) {
		return loadHtml("swagger/swagger-ui.html")
			.replace("${CONTEXT_PATH}", request.getContextPath())
			.replace("${TITLE}", title)
			.replace("${API_SPEC_BASE64}", encodeSpec(request, group));
	}

	private String loadHtml(String resourcePath) {
		try {
			ClassPathResource resource = new ClassPathResource(resourcePath);
			return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load Swagger UI html: " + resourcePath, e);
		}
	}

	private String encodeSpec(HttpServletRequest request, String group) {
		try {
			byte[] specBytes = multipleOpenApiResource.openapiJson(
				request,
				request.getContextPath() + API_DOCS_BASE_PATH,
				group,
				resolveLocale(request)
			);
			return Base64.getEncoder().encodeToString(specBytes);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Failed to render OpenAPI spec for group: " + group, e);
		}
	}

	private Locale resolveLocale(HttpServletRequest request) {
		Locale locale = request.getLocale();
		return locale != null ? locale : Locale.getDefault();
	}
}
