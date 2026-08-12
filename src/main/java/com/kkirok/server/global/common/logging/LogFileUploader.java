package com.kkirok.server.global.common.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogFileUploader {

    private static final String LOG_KEY_PREFIX = "logs";

    private final S3Client r2Client;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${spring.profiles.active:local}")
    private String activeProfile;

    /** 로그 파일 하나를 R2에 업로드한다. 성공하면 true, 실패하면 false를 반환한다. */
    public boolean upload(final File file) {
        String key = buildKey(file);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType("application/gzip")
                .contentLength(file.length())
                .build();

        try {
            r2Client.putObject(putObjectRequest, RequestBody.fromFile(file));
            return true;
        } catch (S3Exception | SdkClientException exception) {
            log.error("로그 파일 업로드 실패: {}", file.getName(), exception);
            return false;
        }
    }

    private String buildKey(final File file) {
        return LOG_KEY_PREFIX + "/" + activeProfile + "/" + LocalDate.now() + "/" + file.getName();
    }
}
