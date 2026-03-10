package com.kkirok.server.global.external.r2.application.service;

import com.kkirok.server.global.external.r2.exception.R2ErrorCode;
import com.kkirok.server.global.external.r2.exception.R2Exception;
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

    public String upload(final MultipartFile file) {
        validateFile(file);
        String key = createKey(file.getOriginalFilename());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        try {
            r2Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException exception) {
            throw new R2Exception(R2ErrorCode.FILE_STREAM_READ_FAILED);
        } catch (software.amazon.awssdk.services.s3.model.S3Exception | SdkClientException exception) {
            log.error("파일 업로드 실패 : ", exception);
            throw new R2Exception(R2ErrorCode.FILE_UPLOAD_FAILED);
        }

        return key;
    }

    private void validateFile(final MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new R2Exception(R2ErrorCode.INVALID_FILE_REQUEST);
        }
    }

    private String createKey(final String originalFilename) {
        String fileName = (originalFilename == null || originalFilename.isBlank())
                ? "file"
                : originalFilename;
        return UUID.randomUUID() + "-" + fileName;
    }
}
