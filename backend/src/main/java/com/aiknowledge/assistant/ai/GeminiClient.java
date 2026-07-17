package com.aiknowledge.assistant.ai;

import com.aiknowledge.assistant.config.AppProperties;
import com.aiknowledge.assistant.exception.AiServiceException;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Thin transport layer over the Gemini {@code generateContent} REST API.
 * Prompt construction lives in {@link com.aiknowledge.assistant.service.AiService};
 * this class only knows the wire format.
 * Transient provider errors (429/5xx, network failures) are retried once and
 * then routed to the configured fallback model, since the shared free-tier
 * pool for a single model can be temporarily saturated.
 */
@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private static final long RETRY_PAUSE_MS = 750;

    private final RestClient restClient;
    private final AppProperties.Gemini gemini;

    public GeminiClient(AppProperties properties, RestClient.Builder restClientBuilder) {
        this.gemini = properties.gemini();
        this.restClient = restClientBuilder.baseUrl(gemini.baseUrl()).build();
    }

    /**
     * @param systemInstruction system prompt applied to the whole call
     * @param turns             alternating conversation turns; role is "user" or "model"
     * @return the model's text reply
     */
    public String generate(String systemInstruction, List<Turn> turns) {
        GenerateContentRequest request = new GenerateContentRequest(
                new Content(null, List.of(new Part(systemInstruction))),
                turns.stream().map(turn -> new Content(turn.role(), List.of(new Part(turn.text())))).toList(),
                new GenerationConfig(0.7, 2048));

        List<String> models = attemptPlan();
        long start = System.currentTimeMillis();
        AiServiceException lastFailure = null;

        for (int attempt = 0; attempt < models.size(); attempt++) {
            String model = models.get(attempt);
            if (attempt > 0) {
                log.warn("Retrying Gemini call with model {} (attempt {}/{})", model, attempt + 1, models.size());
                pause();
            }
            try {
                String text = extractText(call(model, request));
                log.info("Gemini call completed in {} ms ({} turns, model {})",
                        System.currentTimeMillis() - start, turns.size(), model);
                return text;
            } catch (RestClientResponseException ex) {
                log.error("Gemini API returned {} from {}: {}", ex.getStatusCode(), model, ex.getResponseBodyAsString());
                lastFailure = new AiServiceException("Gemini API returned " + ex.getStatusCode(), ex);
                if (!isRetryable(ex)) {
                    break;
                }
            } catch (AiServiceException ex) {
                lastFailure = ex;
            } catch (Exception ex) {
                lastFailure = new AiServiceException("Failed to reach Gemini API", ex);
            }
        }
        throw lastFailure;
    }

    /** Primary model twice, then the fallback model if one is configured. */
    private List<String> attemptPlan() {
        List<String> models = new ArrayList<>(List.of(gemini.model(), gemini.model()));
        String fallback = gemini.fallbackModel();
        if (fallback != null && !fallback.isBlank() && !fallback.equals(gemini.model())) {
            models.add(fallback);
        }
        return models;
    }

    private GenerateContentResponse call(String model, GenerateContentRequest request) {
        return restClient.post()
                .uri("/v1beta/models/{model}:generateContent", model)
                .header("x-goog-api-key", gemini.apiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(GenerateContentResponse.class);
    }

    private static boolean isRetryable(RestClientResponseException ex) {
        int status = ex.getStatusCode().value();
        // 404 included: Google retires model aliases, which should route to the fallback.
        return status == 404 || status == 429 || status >= 500;
    }

    private static void pause() {
        try {
            Thread.sleep(RETRY_PAUSE_MS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private String extractText(GenerateContentResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
            throw new AiServiceException("Gemini returned no candidates");
        }
        Content content = response.candidates().getFirst().content();
        if (content == null || content.parts() == null || content.parts().isEmpty()) {
            throw new AiServiceException("Gemini returned an empty response");
        }
        String text = content.parts().stream()
                .map(Part::text)
                .filter(part -> part != null && !part.isBlank())
                .reduce("", String::concat);
        if (text.isBlank()) {
            throw new AiServiceException("Gemini returned blank text");
        }
        return text.strip();
    }

    public record Turn(String role, String text) {
        public static Turn user(String text) {
            return new Turn("user", text);
        }

        public static Turn model(String text) {
            return new Turn("model", text);
        }
    }

    // --- Gemini wire format ---

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record GenerateContentRequest(Content systemInstruction, List<Content> contents, GenerationConfig generationConfig) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record Content(String role, List<Part> parts) {}

    record Part(String text) {}

    record GenerationConfig(Double temperature, Integer maxOutputTokens) {}

    record GenerateContentResponse(List<Candidate> candidates) {}

    record Candidate(Content content) {}
}
