package com.aiknowledge.assistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Cors cors,
        Gemini gemini,
        Upload upload,
        Ai ai
) {
    public record Jwt(String secret, long expirationMs) {}

    public record Cors(String allowedOrigins) {}

    public record Gemini(String apiKey, String model, String baseUrl) {}

    public record Upload(long maxFileSizeBytes) {}

    public record Ai(int historyMaxMessages, int documentCharBudget) {}
}
