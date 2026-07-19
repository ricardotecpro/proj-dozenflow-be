package com.dozenflow.be.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Aplica a política apenas aos endpoints da API
                // allowedOriginPatterns (em vez de allowedOrigins) permite usar "*" em
                // parte da origem — necessário para os deploy previews do Netlify
                // (ex.: https://deploy-preview-12--dozenflow.netlify.app), que têm um
                // subdomínio diferente a cada PR/branch. Continua compatível com
                // allowCredentials(true) porque nenhum padrão é um "*" literal sozinho.
                .allowedOriginPatterns(allowedOrigins.split(",")) // Permite múltiplas origens
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
