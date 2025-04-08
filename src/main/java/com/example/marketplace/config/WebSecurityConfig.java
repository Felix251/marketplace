package com.example.marketplace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@Configuration
public class WebSecurityConfig {

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api/swagger-ui/**",
                        "/api/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/api/v3/api-docs/**",
                        "/api-docs/**",
                        "/api/api-docs/**"
                );
    }
}