package com.kkirok.server.global.external.r2.application.service;

import com.kkirok.server.global.external.exception.ExternalErrorCode;
import com.kkirok.server.global.external.exception.R2Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class PresignedUrlService {

    private static final int EXPIRE_SECONDS = 300; // 5분

    private final S3Presigner r2Presigner;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${cloudflare.r2.public-base-url:}")
    private String publicBaseUrl;

    public URL getPresignedUrl(final String key) {

        // key 없으면 예외 발생
        if (key == null || key.isBlank()) {
            throw new R2Exception(ExternalErrorCode.R2_INVALID_OBJECT_KEY);
        }

        if (StringUtils.hasText(publicBaseUrl)) {
            return buildPublicUrl(key);
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
            throw new R2Exception(ExternalErrorCode.R2_PRESIGNED_URL_GENERATION_FAILED, exception);
        }
    }

    private URL buildPublicUrl(final String key) {
        try {
            String[] pathSegments = Arrays.stream(key.split("/"))
                    .filter(StringUtils::hasText)
                    .toArray(String[]::new);

            return UriComponentsBuilder.fromHttpUrl(publicBaseUrl)
                    .pathSegment(pathSegments)
                    .build(true)
                    .toUri()
                    .toURL();
        } catch (MalformedURLException exception) {
            log.error("Public URL 생성 실패 : ", exception);
            throw new R2Exception(ExternalErrorCode.R2_PRESIGNED_URL_GENERATION_FAILED, exception);
        }
    }

}
