package com.example.campusshop.config;

import java.util.Arrays;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOriginPatterns;
    private final long maxAge;

    public WebConfig(
            @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:4173}")
            String allowedOrigins,
            @Value("${app.cors.allowed-origin-patterns:}")
            String wildcardPatterns,
            @Value("${app.cors.max-age:3600}")
            long maxAge) {
        // Spring only evaluates origin patterns when no exact origins are configured.
        // Merging both lists here allows an exact allow-list and optional wildcard
        // patterns to be used together without one silently disabling the other.
        this.allowedOriginPatterns = Stream.concat(
                        Arrays.stream(parseCsv(allowedOrigins)),
                        Arrays.stream(parseCsv(wildcardPatterns)))
                .distinct()
                .toArray(String[]::new);
        this.maxAge = maxAge;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        CorsRegistration cors = registry.addMapping("/api/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(maxAge);

        if (allowedOriginPatterns.length > 0) {
            cors.allowedOriginPatterns(allowedOriginPatterns);
        }
    }

    private String[] parseCsv(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toArray(String[]::new);
    }
}
