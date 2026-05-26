package com.kkirok.server.global.common.api;

import com.kkirok.server.global.common.dto.EnumResponse;
import com.kkirok.server.global.common.dto.SuccessResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class EnumControllerTest {

    private final EnumController enumController = new EnumController();

    @Test
    void enums_shouldReturnRegisteredEnumSummariesAndDetails() {
        ResponseEntity<SuccessResponse<EnumResponse>> responseEntity = enumController.enums(null);

        SuccessResponse<EnumResponse> body = responseEntity.getBody();

        assertThat(body).isNotNull();
        assertThat(body.code()).isEqualTo("ENUM_GET_SUCCESS");
        assertThat(body.data().summary()).hasSize(EnumList.values().length);
        assertThat(body.data().details()).hasSize(EnumList.values().length);
    }

    @Test
    void enums_shouldReturnEnumValuesWithDisplayText() {
        EnumResponse response = enumController.enums(null).getBody().data();

        Map<String, String> onboardingPurpose = findDetail(response, EnumList.ONBOARDING_PURPOSE);
        Map<String, String> gender = findDetail(response, EnumList.GENDER);

        assertThat(onboardingPurpose).containsEntry("LOSE_WEIGHT", "감량");
        assertThat(gender).containsEntry("MALE", "MALE");
    }

    @Test
    void enums_shouldReturnOnlySelectedEnumDetail_whenEnumNameIsProvided() {
        EnumResponse response = enumController.enums(EnumList.ONBOARDING_PURPOSE).getBody().data();

        assertThat(response.summary()).hasSize(EnumList.values().length);
        assertThat(response.details()).hasSize(1);
        assertThat(response.details().get(0).enumName()).isEqualTo(EnumList.ONBOARDING_PURPOSE.name());
        assertThat(response.details().get(0).sort()).containsEntry("LOSE_WEIGHT", "감량");
    }

    private Map<String, String> findDetail(EnumResponse response, EnumList enumList) {
        return response.details().stream()
                .filter(detail -> detail.enumName().equals(enumList.name()))
                .findFirst()
                .orElseThrow()
                .sort();
    }
}
