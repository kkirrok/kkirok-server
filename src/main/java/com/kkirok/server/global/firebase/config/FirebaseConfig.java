package com.kkirok.server.global.firebase.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kkirok.server.domain.notification.exception.NotificationErrorCode;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "firebase", name = "credential-path")
public class FirebaseConfig {

    private final FirebaseProperties firebaseProperties;
    private final ResourceLoader resourceLoader;

    @Bean
    public FirebaseApp firebaseApp() {
        GoogleCredentials credentials = resolveCredentials();
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    private GoogleCredentials resolveCredentials() {
        String credentialPath = firebaseProperties.getCredentialPath();
        if (credentialPath == null || credentialPath.isBlank()) {
            throw new IllegalStateException(NotificationErrorCode.FCM_INIT_FAILED.getMessage());
        }

        Resource resource = resourceLoader.getResource(credentialPath);
        if (!resource.exists()) {
            throw new IllegalStateException(NotificationErrorCode.FCM_INIT_FAILED.getMessage());
        }

        try (InputStream inputStream = resource.getInputStream()) {
            return GoogleCredentials.fromStream(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException(NotificationErrorCode.FCM_INIT_FAILED.getMessage(), e);
        }
    }
}
