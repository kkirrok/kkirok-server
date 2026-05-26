package com.kkirok.server.global.common.api;

import com.kkirok.server.global.common.dto.EnumResponse;
import com.kkirok.server.global.common.dto.SuccessResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Enum API", description = "API 호출에 필요한 Enum 정보 관련 API")
@RestController
@RequestMapping("/v1/enums")
public class EnumController {

    @GetMapping
    @Operation(summary = "Enum 정보 조회 API", description = "API 호출에 필요한 enum 정보를 반환합니다.<br>enum 종류와 enum 종류별 값을 조회할 수 있습니다.<br>필요한 데이터 부재 시 문의 부탁드립니다.")
    public ResponseEntity<SuccessResponse<EnumResponse>> enums(
            @Parameter(description = "조회할 enum 종류. 미입력 시 전체 enum 값을 반환합니다.")
            @RequestParam(required = false) EnumList enumName
    ) {
        List<EnumResponse.EnumSummary> summary = Arrays.stream(EnumList.values())
                .map(enumList -> new EnumResponse.EnumSummary(
                        enumList.name(),
                        enumList.getDescription()
                ))
                .toList();

        List<EnumList> targetEnums = enumName == null ? List.of(EnumList.values()) : List.of(enumName);

        List<EnumResponse.EnumDetail> details = targetEnums.stream()
                .map(enumList -> new EnumResponse.EnumDetail(
                        enumList.name(),
                        toEnumMap(enumList.getEnumClass())
                ))
                .toList();

        EnumResponse response = new EnumResponse(summary, details);

        return ResponseEntity.ok(new SuccessResponse<>("ENUM_GET_SUCCESS", 200, "enum 목록 조회 성공", response));
    }

    private Map<String, String> toEnumMap(Class<?> enumClass) {
        Map<String, String> values = new LinkedHashMap<>();

        for (Object enumConstant : enumClass.getEnumConstants()) {
            Enum<?> enumValue = (Enum<?>) enumConstant;
            values.put(enumValue.name(), getDisplayValue(enumValue));
        }

        return values;
    }

    private String getDisplayValue(Enum<?> enumValue) {
        return Arrays.stream(new String[]{"getLabel", "getType", "getRoleName", "getCode"})
                .map(methodName -> invokeStringGetter(enumValue, methodName))
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(enumValue.name());
    }

    private String invokeStringGetter(Enum<?> enumValue, String methodName) {
        try {
            Method method = enumValue.getClass().getMethod(methodName);
            Object value = method.invoke(enumValue);
            return value instanceof String stringValue ? stringValue : null;
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }
}
