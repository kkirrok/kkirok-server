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
			.description("살빼자")
			.version("0.0.1");
	}
}
