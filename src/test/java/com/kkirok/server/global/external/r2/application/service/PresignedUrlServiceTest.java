package com.kkirok.server.global.external.r2.application.service;

import com.kkirok.server.global.external.r2.exception.R2ErrorCode;
import com.kkirok.server.global.external.r2.exception.R2Exception;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PresignedUrlServiceTest {

    @Mock
    private S3Presigner r2Presigner;

    @InjectMocks
    private PresignedUrlService presignedUrlService;

    @Test
    @DisplayName("유효한 객체 키가 전달되면 다운로드용 Presigned URL을 생성한다")
    void shouldReturnPresignedUrl_whenKeyIsValid() throws MalformedURLException {
        // Given
        ReflectionTestUtils.setField(presignedUrlService, "bucketName", "test-bucket");
        PresignedGetObjectRequest presignedRequest = org.mockito.Mockito.mock(PresignedGetObjectRequest.class);
        URL expectedUrl = new URL("http://localhost:9000/test-bucket/profile.png");

        given(r2Presigner.presignGetObject(any(GetObjectPresignRequest.class))).willReturn(presignedRequest);
        given(presignedRequest.url()).willReturn(expectedUrl);

        // When
        URL result = presignedUrlService.getPresignedUrl("profile.png");

        // Then
        assertThat(result).isEqualTo(expectedUrl);

        ArgumentCaptor<GetObjectPresignRequest> requestCaptor =
                ArgumentCaptor.forClass(GetObjectPresignRequest.class);
        then(r2Presigner).should().presignGetObject(requestCaptor.capture());

        GetObjectRequest objectRequest = requestCaptor.getValue().getObjectRequest();
        assertThat(objectRequest.bucket()).isEqualTo("test-bucket");
        assertThat(objectRequest.key()).isEqualTo("profile.png");
    }

    @Test
    @DisplayName("비어 있는 객체 키로는 Presigned URL을 생성할 수 없다")
    void shouldThrowR2Exception_whenKeyIsBlank() {
        // Given
        ReflectionTestUtils.setField(presignedUrlService, "bucketName", "test-bucket");

        // When, Then
        assertThatThrownBy(() -> presignedUrlService.getPresignedUrl(" "))
                .isInstanceOf(R2Exception.class)
                .extracting("baseErrorCode")
                .isEqualTo(R2ErrorCode.INVALID_OBJECT_KEY);
    }

    @Test
    @DisplayName("R2 Presigner 호출에 실패하면 Presigned URL을 생성할 수 없다")
    void shouldThrowR2Exception_whenPresignerFails() {
        // Given
        ReflectionTestUtils.setField(presignedUrlService, "bucketName", "test-bucket");
        given(r2Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .willThrow(SdkClientException.create("presign failed"));

        // When, Then
        assertThatThrownBy(() -> presignedUrlService.getPresignedUrl("profile.png"))
                .isInstanceOf(R2Exception.class)
                .extracting("baseErrorCode")
                .isEqualTo(R2ErrorCode.PRESIGNED_URL_GENERATION_FAILED);
    }
}
