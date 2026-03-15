package com.kkirok.server.domain.member.application.service;

import com.kkirok.server.domain.member.exception.EmailErrorCode;
import com.kkirok.server.domain.member.exception.EmailException;
import com.kkirok.server.domain.member.util.EmailTemplateGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

/**
 * Amazon SES 전송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SesEmailSender implements EmailSender {

    private final SesClient sesClient;

    @Value("${aws.ses.source-email}")
    private String sourceEmail;

    /**
     * email에 인증코드(code)를 발송합니다.
     * @param email 발송할 이메일
     * @param code 인증코드
     */
    @Override
    public void sendVerificationCode(final String email, final String code) {
        String htmlMessage = EmailTemplateGenerator.renderVerificationCodeTemplate(code);

        SendEmailRequest request = SendEmailRequest.builder()
                .source(sourceEmail)
                .destination(Destination.builder()
                        .toAddresses(email)
                        .build())
                .message(Message.builder()
                        .subject(Content.builder()
                                .charset("UTF-8")
                                .data("[Kkirok] 이메일 인증번호 안내")
                                .build())
                        .body(Body.builder()
                                .html(Content.builder()
                                        .charset("UTF-8")
                                        .data(htmlMessage)
                                        .build())
                                .build())
                        .build())
                .build();

        try {
            sesClient.sendEmail(request);
        } catch (SesException | SdkClientException exception) {
            log.error("SES email send failed. email={}", email, exception);
            throw new EmailException(EmailErrorCode.EMAIL_SEND_FAILED);
        }
    }
}
