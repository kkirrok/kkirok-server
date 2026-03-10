package com.kkirok.server.global.external.r2.application.service;

import com.kkirok.server.global.external.r2.exception.R2ErrorCode;
import com.kkirok.server.global.external.r2.exception.R2Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URL;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresignedUrlService {

    private static final int EXPIRE_SECONDS = 300; // 5분

    private final S3Presigner r2Presigner;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    public URL getPresignedUrl(final String key) {

        // key 없으면 예외 발생
        if (key == null || key.isBlank()) {
            throw new R2Exception(R2ErrorCode.INVALID_OBJECT_KEY);
        }

        // key와 버킷으로 이미지 url 조회
        try {
            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(EXPIRE_SECONDS))
                    .getObjectRequest(objectRequest)
                    .build();

            return r2Presigner.presignGetObject(presignRequest).url();
        } catch (software.amazon.awssdk.services.s3.model.S3Exception | SdkClientException exception) {
            log.error("Presigned URL 발급 실패 : ", exception);
            throw new R2Exception(R2ErrorCode.PRESIGNED_URL_GENERATION_FAILED);
        }
    }

}
