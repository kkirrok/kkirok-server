package com.kkirok.server.global.swagger.config;

import com.kkirok.server.global.swagger.ExampleHolder;
import com.kkirok.server.global.common.exception.base.BaseErrorCode;
import com.kkirok.server.global.common.exception.base.BaseSuccessCode;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExample;
import com.kkirok.server.global.swagger.annotation.ApiErrorCodeExamples;
import com.kkirok.server.global.swagger.annotation.ApiSuccessCodeExample;
import com.kkirok.server.global.swagger.annotation.ApiSuccessCodeExamples;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 커스텀 에러 코드 어노테이션을 Swagger 응답 예시에 반영하는 설정.
 */
@Configuration
public class SwaggerErrorExampleConfig {

    private static final String SUCCESS_EXAMPLES_EXTENSION = "x-success-examples";

    @Bean
    public OperationCustomizer errorCodeExampleCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            if (operation.getResponses() == null) {
                operation.setResponses(new ApiResponses());
            }

            List<ApiSuccessCodeExample> successCodeExamples = collectSuccessCodeExamples(handlerMethod);
            for (ApiSuccessCodeExample success : successCodeExamples) {
                ResolvedSuccessCode resolvedSuccessCode = resolveSuccessCode(success);
                if (resolvedSuccessCode != null) {
                    applySuccessExample(operation, resolvedSuccessCode.code(), resolvedSuccessCode.status(), resolvedSuccessCode.description());
                    addSuccessExampleExtension(operation, resolvedSuccessCode);
                }
            }

            List<ApiErrorCodeExample> errorCodeExamples = collectErrorCodeExamples(handlerMethod);
            for (int i = 0; i < errorCodeExamples.size(); i++) {
                ApiErrorCodeExample errorCodeExample = errorCodeExamples.get(i);
                ExampleHolder holder = toExampleHolder(errorCodeExample, i + 1);
                if (holder == null) {
                    continue;
                }
                String statusCode = String.valueOf(holder.getCode());

                ApiResponse apiResponse = operation.getResponses().get(statusCode);
                if (apiResponse == null) {
                    String description = holder.getDescription();
                    apiResponse = new ApiResponse().description(description);
                    operation.getResponses().addApiResponse(statusCode, apiResponse);
                } else if (apiResponse.getDescription() == null || apiResponse.getDescription().isBlank()) {
                    apiResponse.setDescription(holder.getDescription());
                }

                if (apiResponse.getContent() == null) {
                    apiResponse.setContent(new Content());
                }

                MediaType mediaType = getOrCreateJsonMediaType(apiResponse);

                if (mediaType.getExamples() == null) {
                    mediaType.setExamples(new LinkedHashMap<>());
                }

                mediaType.getExamples().put(holder.getName(), holder.getHolder());
            }

            return operation;
        };
    }

    @Bean
    public OpenApiCustomizer successResponseExampleCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }

            openApi.getPaths().values().forEach(pathItem ->
                    pathItem.readOperations().forEach(operation -> applySuccessExamplesFromExtension(openApi, operation)));
        };
    }

    private void applySuccessExample(Operation operation, String code, int status, String description) {
        String statusCode = String.valueOf(status);
        ApiResponse apiResponse = operation.getResponses().get(statusCode);
        if (apiResponse == null) {
            apiResponse = new ApiResponse().description(description);
            operation.getResponses().addApiResponse(statusCode, apiResponse);
        } else if (apiResponse.getDescription() == null || apiResponse.getDescription().isBlank()) {
            apiResponse.setDescription(description);
        }
    }

    @SuppressWarnings("unchecked")
    private void addSuccessExampleExtension(Operation operation, ResolvedSuccessCode successCode) {
        if (operation.getExtensions() == null) {
            operation.setExtensions(new LinkedHashMap<>());
        }

        Map<String, Object> successExamples = (Map<String, Object>) operation.getExtensions()
                .computeIfAbsent(SUCCESS_EXAMPLES_EXTENSION, key -> new LinkedHashMap<String, Object>());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("code", successCode.code());
        payload.put("status", successCode.status());
        payload.put("message", successCode.description());

        successExamples.put(String.valueOf(successCode.status()), payload);
    }

    private List<ApiErrorCodeExample> collectErrorCodeExamples(HandlerMethod handlerMethod) {
        List<ApiErrorCodeExample> examples = new ArrayList<>();

        ApiErrorCodeExample singleOnMethod = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), ApiErrorCodeExample.class);
        if (singleOnMethod != null) {
            examples.add(singleOnMethod);
        }

        ApiErrorCodeExamples multiOnMethod = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), ApiErrorCodeExamples.class);
        if (multiOnMethod != null) {
            examples.addAll(Arrays.asList(multiOnMethod.value()));
        }

        // Controller가 docs 인터페이스를 구현하는 구조에서 인터페이스 메서드 어노테이션도 수집한다.
        for (Class<?> interfaceType : handlerMethod.getBeanType().getInterfaces()) {
            try {
                Method interfaceMethod = interfaceType.getMethod(
                        handlerMethod.getMethod().getName(),
                        handlerMethod.getMethod().getParameterTypes()
                );

                ApiErrorCodeExample singleOnInterface = AnnotatedElementUtils.findMergedAnnotation(
                        interfaceMethod, ApiErrorCodeExample.class);
                if (singleOnInterface != null) {
                    examples.add(singleOnInterface);
                }

                ApiErrorCodeExamples multiOnInterface = AnnotatedElementUtils.findMergedAnnotation(
                        interfaceMethod, ApiErrorCodeExamples.class);
                if (multiOnInterface != null) {
                    examples.addAll(Arrays.asList(multiOnInterface.value()));
                }
            } catch (NoSuchMethodException ignored) {
                // no-op
            }
        }

        Map<String, ApiErrorCodeExample> dedup = new LinkedHashMap<>();
        for (ApiErrorCodeExample item : examples) {
            String key = item.codeType().getName() + "|" + item.code() + "|" + item.status() + "|" + item.message();
            dedup.putIfAbsent(key, item);
        }
        return new ArrayList<>(dedup.values());
    }

    private List<ApiSuccessCodeExample> collectSuccessCodeExamples(HandlerMethod handlerMethod) {
        List<ApiSuccessCodeExample> results = new ArrayList<>();

        ApiSuccessCodeExample singleOnMethod = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), ApiSuccessCodeExample.class);
        if (singleOnMethod != null) {
            results.add(singleOnMethod);
        }

        ApiSuccessCodeExamples multiOnMethod = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), ApiSuccessCodeExamples.class);
        if (multiOnMethod != null) {
            results.addAll(Arrays.asList(multiOnMethod.value()));
        }

        for (Class<?> interfaceType : handlerMethod.getBeanType().getInterfaces()) {
            try {
                Method interfaceMethod = interfaceType.getMethod(
                        handlerMethod.getMethod().getName(),
                        handlerMethod.getMethod().getParameterTypes()
                );

                ApiSuccessCodeExample singleOnInterface = AnnotatedElementUtils.findMergedAnnotation(
                        interfaceMethod, ApiSuccessCodeExample.class);
                if (singleOnInterface != null) {
                    results.add(singleOnInterface);
                }

                ApiSuccessCodeExamples multiOnInterface = AnnotatedElementUtils.findMergedAnnotation(
                        interfaceMethod, ApiSuccessCodeExamples.class);
                if (multiOnInterface != null) {
                    results.addAll(Arrays.asList(multiOnInterface.value()));
                }
            } catch (NoSuchMethodException ignored) {
                // no-op
            }
        }

        Map<String, ApiSuccessCodeExample> dedup = new LinkedHashMap<>();
        for (ApiSuccessCodeExample item : results) {
            String key = item.codeType().getName() + "|" + item.code() + "|" + item.status() + "|" + item.description();
            dedup.putIfAbsent(key, item);
        }
        return new ArrayList<>(dedup.values());
    }

    private ExampleHolder toExampleHolder(ApiErrorCodeExample errorCodeExample, int defaultIndex) {
        ResolvedErrorCode resolvedErrorCode = resolveErrorCode(errorCodeExample);
        if (resolvedErrorCode == null) {
            return null;
        }

        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("code", resolvedErrorCode.code());
        errorBody.put("status", resolvedErrorCode.status());
        errorBody.put("message", resolvedErrorCode.message());

        Example example = new Example();
        example.setValue(errorBody);

        String name = resolvedErrorCode.exampleName();
        if (name == null || name.isBlank()) {
            name = "ERROR_" + resolvedErrorCode.status() + "_" + defaultIndex;
        }

        return ExampleHolder.builder()
                .name(name)
                .code(resolvedErrorCode.status())
                .description(resolvedErrorCode.message())
                .holder(example)
                .build();
    }

    private ResolvedErrorCode resolveErrorCode(ApiErrorCodeExample item) {
        BaseErrorCode enumCode = resolveEnumCode(item.codeType(), item.code(), BaseErrorCode.class);
        if (enumCode != null) {
            String message = isBlank(item.message()) ? enumCode.getMessage() : item.message();
            String code = item.code();
            String exampleName = isBlank(item.exampleName()) ? code : item.exampleName();
            return new ResolvedErrorCode(code, enumCode.getStatus(), message, exampleName);
        }

        if (item.status() > 0) {
            String message = isBlank(item.message()) ? "에러" : item.message();
            String code = isBlank(item.exampleName()) ? "UNDEFINED_ERROR" : item.exampleName();
            return new ResolvedErrorCode(code, item.status(), message, item.exampleName());
        }
        return null;
    }

    private ResolvedSuccessCode resolveSuccessCode(ApiSuccessCodeExample item) {
        BaseSuccessCode enumCode = resolveEnumCode(item.codeType(), item.code(), BaseSuccessCode.class);
        if (enumCode != null) {
            String description = isBlank(item.description()) ? enumCode.getMessage() : item.description();
            return new ResolvedSuccessCode(item.code(), enumCode.getStatus(), description);
        }

        if (item.status() > 0) {
            String description = isBlank(item.description()) ? "성공" : item.description();
            String code = isBlank(item.exampleName()) ? "SUCCESS_" + item.status() : item.exampleName();
            return new ResolvedSuccessCode(code, item.status(), description);
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private <T> T resolveEnumCode(Class<?> codeType, String codeName, Class<T> targetType) {
        if (codeType == null || codeType == targetType || isBlank(codeName) || !codeType.isEnum()) {
            return null;
        }
        Class<? extends Enum> enumClass = (Class<? extends Enum>) codeType.asSubclass(Enum.class);
        try {
            Object enumConstant = Enum.valueOf(enumClass, codeName);
            return targetType.cast(enumConstant);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private MediaType getOrCreateJsonMediaType(ApiResponse apiResponse) {
        Content content = apiResponse.getContent();
        MediaType mediaType = content.get("application/json");
        if (mediaType != null) {
            return mediaType;
        }

        MediaType charsetJson = content.get("application/json;charset=UTF-8");
        if (charsetJson != null) {
            return charsetJson;
        }

        for (Map.Entry<String, MediaType> entry : content.entrySet()) {
            if (entry.getKey() != null && entry.getKey().startsWith("application/json")) {
                return entry.getValue();
            }
        }

        MediaType created = new MediaType();
        content.addMediaType("application/json", created);
        return created;
    }

    @SuppressWarnings("unchecked")
    private void applySuccessExamplesFromExtension(OpenAPI openApi, Operation operation) {
        if (operation.getExtensions() == null) {
            return;
        }

        Object rawExamples = operation.getExtensions().get(SUCCESS_EXAMPLES_EXTENSION);
        if (!(rawExamples instanceof Map<?, ?> successExamples)) {
            return;
        }

        if (operation.getResponses() == null) {
            return;
        }

        for (Map.Entry<?, ?> entry : successExamples.entrySet()) {
            String statusCode = String.valueOf(entry.getKey());
            Object value = entry.getValue();
            if (!(value instanceof Map<?, ?> exampleMap)) {
                continue;
            }

            ApiResponse apiResponse = operation.getResponses().get(statusCode);
            if (apiResponse == null) {
                continue;
            }

            Object code = exampleMap.get("code");
            Object status = exampleMap.get("status");
            Object message = exampleMap.get("message");
            if (!(code instanceof String codeValue) || !(status instanceof Number statusValue)
                    || !(message instanceof String messageValue)) {
                continue;
            }

            applyResponseWrapperExamples(openApi, apiResponse, codeValue, statusValue.intValue(), messageValue);
        }
    }

    private void applyResponseWrapperExamples(OpenAPI openApi, ApiResponse apiResponse, String code, int status, String message) {
        if (apiResponse.getContent() == null) {
            return;
        }

        MediaType mediaType = getOrCreateJsonMediaType(apiResponse);
        if (mediaType == null || mediaType.getSchema() == null) {
            return;
        }

        Schema<?> schema = resolveSchema(openApi, mediaType.getSchema());
        applySchemaExamples(schema, code, status, message);
    }

    @SuppressWarnings("unchecked")
    private void applySchemaExamples(Schema<?> schema, String code, int status, String message) {
        if (schema == null) {
            return;
        }

        if (schema.getProperties() != null) {
            Object codeProp = schema.getProperties().get("code");
            if (codeProp instanceof Schema<?> codeSchema) {
                codeSchema.setExample(code);
            }

            Object statusProp = schema.getProperties().get("status");
            if (statusProp instanceof Schema<?> statusSchema) {
                statusSchema.setExample(status);
            }

            Object messageProp = schema.getProperties().get("message");
            if (messageProp instanceof Schema<?> messageSchema) {
                messageSchema.setExample(message);
            }
        }

        if (schema.getAllOf() != null) {
            for (Schema<?> item : schema.getAllOf()) {
                applySchemaExamples(item, code, status, message);
            }
        }
        if (schema.getOneOf() != null) {
            for (Schema<?> item : schema.getOneOf()) {
                applySchemaExamples(item, code, status, message);
            }
        }
        if (schema.getAnyOf() != null) {
            for (Schema<?> item : schema.getAnyOf()) {
                applySchemaExamples(item, code, status, message);
            }
        }
    }

    private Schema<?> resolveSchema(OpenAPI openApi, Schema<?> schema) {
        if (schema == null) {
            return null;
        }

        if (schema.get$ref() != null && openApi.getComponents() != null && openApi.getComponents().getSchemas() != null) {
            String ref = schema.get$ref();
            String prefix = "#/components/schemas/";
            if (ref.startsWith(prefix)) {
                Schema<?> resolved = openApi.getComponents().getSchemas().get(ref.substring(prefix.length()));
                if (resolved != null) {
                    return resolved;
                }
            }
        }

        return schema;
    }

    private record ResolvedErrorCode(String code, int status, String message, String exampleName) {
    }

    private record ResolvedSuccessCode(String code, int status, String description) {
    }
}
