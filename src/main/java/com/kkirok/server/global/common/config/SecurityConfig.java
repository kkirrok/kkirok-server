package com.kkirok.server.global.common.config;

import com.kkirok.server.domain.user.domain.Role;
import com.kkirok.server.global.auth.jwt.filter.JwtAuthenticationFilter;
import com.kkirok.server.global.auth.security.CustomAccessDeniedHandler;
import com.kkirok.server.global.auth.security.CustomJwtAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@EnableWebSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomJwtAuthenticationEntryPoint customJwtAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    public String[] getAuthWhitelist() {
        return new String[] {
                "/v1/users/login/**",
                "/v1/users/sign-up/local",
                "/v1/users/refresh-token",
                "/v1/main",
                "/api-docs/**",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/api/files/**",
                "/v1/users/email-verification/send",
                "/v1/users/email-verification/verify",
                "/error",
//                actuatorEndPoint + "/health",
//                actuatorEndPoint + "/prometheus", // TODO: 추후 모니터링 추가하면 활성화
                "/"
        };
    }

    private static final String[] AUTH_ADMIN_ONLY = {
            "/api/admin/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(customJwtAuthenticationEntryPoint)
                                .accessDeniedHandler(customAccessDeniedHandler));

        http.authorizeHttpRequests(auth ->
                        auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/admin/login").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/admin/account-requests").permitAll()
                                .requestMatchers("/swagger-ui/index.html", "/swagger-ui.html").denyAll()
                                .requestMatchers("/api-specs/**").denyAll()
                                .requestMatchers(getAuthWhitelist()).permitAll()
                                .requestMatchers(AUTH_ADMIN_ONLY).hasAuthority(Role.ADMIN.getRoleName())
                                .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
