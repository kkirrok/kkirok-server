package com.kkirok.server.global.external.r2.application.service;

import com.kkirok.server.global.external.exception.ExternalErrorCode;
import com.kkirok.server.global.external.exception.R2Exception;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class R2UploadServiceTest {

    @Mock
    private S3Client r2Client;

    @InjectMocks
    private R2UploadService r2UploadService;

    @Test
    @DisplayName("유효한 파일을 업로드하면 R2 객체 키를 반환한다")
    void shouldReturnObjectKey_whenUploadSucceeds() {
        // Given
        ReflectionTestUtils.setField(r2UploadService, "bucketName", "test-bucket");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.png",
                "image/png",
                "test-image".getBytes()
        );

        // When
        String key = r2UploadService.upload(file);

        // Then
        assertThat(key).endsWith("-profile.png");

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        then(r2Client).should().putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest request = requestCaptor.getValue();
        assertThat(request.bucket()).isEqualTo("test-bucket");
        assertThat(request.key()).isEqualTo(key);
        assertThat(request.contentType()).isEqualTo("image/png");
        assertThat(request.contentLength()).isEqualTo(file.getSize());
    }

    @Test
    @DisplayName("파일 이름이 없으면 기본 파일 이름으로 업로드 키를 생성한다")
    void shouldCreateDefaultFileName_whenOriginalFilenameIsMissing() {
        // Given
        ReflectionTestUtils.setField(r2UploadService, "bucketName", "test-bucket");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "",
                "image/png",
                "test-image".getBytes()
        );

        // When
        String key = r2UploadService.upload(file);

        // Then
        assertThat(key).endsWith("-file");
    }

    @Test
    @DisplayName("비어 있는 파일로는 업로드할 수 없다")
    void shouldThrowR2Exception_whenFileIsEmpty() {
        // Given
        ReflectionTestUtils.setField(r2UploadService, "bucketName", "test-bucket");
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        // When, Then
        assertThatThrownBy(() -> r2UploadService.upload(emptyFile))
                .isInstanceOf(R2Exception.class)
                .extracting("baseErrorCode")
                .isEqualTo(ExternalErrorCode.R2_INVALID_FILE_REQUEST);
    }

    @Test
    @DisplayName("파일 스트림을 읽을 수 없으면 업로드를 진행할 수 없다")
    void shouldThrowR2Exception_whenFileStreamReadFails() throws IOException {
        // Given
        ReflectionTestUtils.setField(r2UploadService, "bucketName", "test-bucket");
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        org.mockito.BDDMockito.given(file.isEmpty()).willReturn(false);
        org.mockito.BDDMockito.given(file.getOriginalFilename()).willReturn("profile.png");
        org.mockito.BDDMockito.given(file.getContentType()).willReturn("image/png");
        org.mockito.BDDMockito.given(file.getSize()).willReturn(10L);
        org.mockito.BDDMockito.given(file.getInputStream()).willThrow(new IOException("stream read failed"));

        // When, Then
        assertThatThrownBy(() -> r2UploadService.upload(file))
                .isInstanceOf(R2Exception.class)
                .extracting("baseErrorCode")
                .isEqualTo(ExternalErrorCode.R2_FILE_STREAM_READ_FAILED);
    }

    @Test
    @DisplayName("R2 업로드 중 외부 스토리지 오류가 발생하면 업로드에 실패한다")
    void shouldThrowR2Exception_whenR2ClientThrowsSdkException() {
        // Given
        ReflectionTestUtils.setField(r2UploadService, "bucketName", "test-bucket");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "profile.png",
                "image/png",
                "test-image".getBytes()
        );
        doThrow(SdkClientException.create("upload failed"))
                .when(r2Client)
                .putObject(any(PutObjectRequest.class), any(RequestBody.class));

        // When, Then
        assertThatThrownBy(() -> r2UploadService.upload(file))
                .isInstanceOf(R2Exception.class)
                .extracting("baseErrorCode")
                .isEqualTo(ExternalErrorCode.R2_FILE_UPLOAD_FAILED);
    }
}
