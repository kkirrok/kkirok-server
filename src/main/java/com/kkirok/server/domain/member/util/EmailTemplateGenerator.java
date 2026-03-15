package com.kkirok.server.domain.member.util;

import com.kkirok.server.domain.member.exception.EmailErrorCode;
import com.kkirok.server.domain.member.exception.EmailException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class EmailTemplateGenerator {

    private static final String EMAIL_VERIFICATION_TEMPLATE_PATH = "templates/email-verification.html";
    private static final String VERIFICATION_CODE_PLACEHOLDER = "${verificationCode}";

    public static String renderVerificationCodeTemplate(final String code) {
        try (InputStream inputStream = new ClassPathResource(EMAIL_VERIFICATION_TEMPLATE_PATH).getInputStream()) {
            String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return template.replace(VERIFICATION_CODE_PLACEHOLDER, code);
        } catch (IOException exception) {
            log.error("Failed to render email template. path={}", EMAIL_VERIFICATION_TEMPLATE_PATH, exception);
            throw new EmailException(EmailErrorCode.EMAIL_SEND_FAILED);
        }
    }
}
