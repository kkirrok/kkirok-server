package com.kkirok.server.global.swagger.config;

import com.kkirok.server.global.swagger.annotation.DisableSwaggerSecurity;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class SwaggerConfig {

	@Value("${app.server.url}")
	private String serverUrl;

	@Bean
	public OpenAPI openAPI() {
		String jwt = "JWT";
		SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);

		Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme()
			.name(jwt)
			.type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT")
		);

		return new OpenAPI()
			.addServersItem(new Server().url(serverUrl))
			.components(components)
			.info(apiInfo())
			.addSecurityItem(securityRequirement);
	}

	@Bean
	public GroupedOpenApi generalApi(
		@Qualifier("customize") OperationCustomizer securityCustomizer,
		@Qualifier("errorCodeExampleCustomizer") OperationCustomizer errorCodeExampleCustomizer,
		@Qualifier("snakeCaseSchemaCustomizer") OpenApiCustomizer snakeCaseSchemaCustomizer,
		@Qualifier("successResponseExampleCustomizer") OpenApiCustomizer successResponseExampleCustomizer
	) {
		return GroupedOpenApi.builder()
			.group("general")
			.pathsToMatch("/**")
			.pathsToExclude("/api/admin/**")
			.addOperationCustomizer(securityCustomizer)
			.addOperationCustomizer(errorCodeExampleCustomizer)
			.addOpenApiCustomizer(openApi -> openApi.info(apiInfo("Kkirok OpenAPI Docs")))
			.addOpenApiCustomizer(snakeCaseSchemaCustomizer)
			.addOpenApiCustomizer(successResponseExampleCustomizer)
			.build();
	}

	@Bean
	public GroupedOpenApi adminApi(
		@Qualifier("customize") OperationCustomizer securityCustomizer,
		@Qualifier("errorCodeExampleCustomizer") OperationCustomizer errorCodeExampleCustomizer,
		@Qualifier("snakeCaseSchemaCustomizer") OpenApiCustomizer snakeCaseSchemaCustomizer,
		@Qualifier("successResponseExampleCustomizer") OpenApiCustomizer successResponseExampleCustomizer
	) {
		return GroupedOpenApi.builder()
			.group("admin")
			.pathsToMatch("/api/admin/**")
			.addOperationCustomizer(securityCustomizer)
			.addOperationCustomizer(errorCodeExampleCustomizer)
			.addOpenApiCustomizer(openApi -> openApi.info(apiInfo("Kkirok Admin API Docs")))
			.addOpenApiCustomizer(snakeCaseSchemaCustomizer)
			.addOpenApiCustomizer(successResponseExampleCustomizer)
			.build();
	}

	@Bean
	public OperationCustomizer customize() {
		return (operation, handlerMethod) -> {
			DisableSwaggerSecurity methodAnnotation = handlerMethod.getMethodAnnotation(DisableSwaggerSecurity.class);
			if (methodAnnotation != null) {
				operation.setSecurity(Collections.emptyList());
			}
			return operation;
		};
	}

	private Info apiInfo() {
		return apiInfo("Kkirok Project API");
	}

	private Info apiInfo(String title) {
		return new Info()
			.title(title)
			.description("""
					### 끼록 API 명세서입니다.

					각 API 문서에서 HTTP Method, Endpoint, API 이름, 필요한 권한을 함께 확인할 수 있습니다.

					### API 기본 정보
					- `GET /v1/meals` 식단 조회 `[USER]`
					- `PATCH /api/admin/account-requests/{requestId}/status` 관리자 계정 요청 상태 처리 `[ADMIN]`
					- 권한이 필요 없는 API는 `[]` 로 표시됩니다.

					즉, 대괄호 안의 값은 해당 API를 호출하기 위해 필요한 권한을 의미합니다.
					
					### API 응답정보
					각 API에는 응답으로 나올 수 있는 응답코드 종류와 각 예시를 확인할 수 있습니다. 참고 바랍니다.
					
					""")
			.version("0.0.1");
	}
}
