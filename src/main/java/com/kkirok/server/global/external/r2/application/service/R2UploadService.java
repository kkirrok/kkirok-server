package com.kkirok.server.global.external.r2.application.service;

import com.kkirok.server.global.external.exception.ExternalErrorCode;
import com.kkirok.server.global.external.exception.R2Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class R2UploadService {

    private final S3Client r2Client;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    public String upload(final MultipartFile file, final R2UploadType uploadType) {
        validateFile(file);
        String key = createKey(uploadType, file.getOriginalFilename());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        try {
            r2Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException exception) {
            throw new R2Exception(ExternalErrorCode.R2_FILE_STREAM_READ_FAILED, exception);
        } catch (software.amazon.awssdk.services.s3.model.S3Exception | SdkClientException exception) {
            log.error("파일 업로드 실패 : ", exception);
            throw new R2Exception(ExternalErrorCode.R2_FILE_UPLOAD_FAILED, exception);
        }

        return key;
    }

    private void validateFile(final MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new R2Exception(ExternalErrorCode.R2_INVALID_FILE_REQUEST);
        }
    }

    private String createKey(final R2UploadType uploadType, final String originalFilename) {
        String extension = extractExtension(originalFilename);
        return uploadType.getPrefix() + "/" + UUID.randomUUID() + extension;
    }

    private String extractExtension(final String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }

        String extension = filename.substring(filename.lastIndexOf("."));
        return extension.matches("\\.[a-zA-Z0-9]+") ? extension : "";
    }
}
