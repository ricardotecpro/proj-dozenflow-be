package com.dozenflow.be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(RateLimitFilter rateLimitFilter) {
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Desabilita a proteção CSRF, que é a causa do erro 403 em APIs stateless
            .csrf(csrf -> csrf.disable())

            // Define as regras de autorização para os endpoints
            .authorizeHttpRequests(auth -> auth
                // Permite acesso sem autenticação a todos os endpoints da API e do Swagger
                .requestMatchers("/api/**", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/health").permitAll()
                // Qualquer outra requisição deve ser permitida (para o RootController, etc.)
                .anyRequest().permitAll()
            )

            // Aplica a configuração de CORS definida na classe WebConfig
            .cors(withDefaults())

            // Security headers explícitos (alguns já vêm por padrão no Spring Security,
            // mas ficam declarados aqui para não depender de defaults implícitos).
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(withDefaults())
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000))
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            )

            // Rate limiting básico por IP nos endpoints /api/**
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
