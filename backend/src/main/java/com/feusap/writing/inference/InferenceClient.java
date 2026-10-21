package com.feusap.writing.inference;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * HTTP client for the FastAPI inference service.
 * Timeouts come from spring.http.client.* (see application.yml).
 */
@Component
public class InferenceClient {

    private final RestClient restClient;

    public InferenceClient(RestClient.Builder builder, @Value("${inference.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public AnalysisResult analyze(String text) {
        try {
            AnalysisResult result = restClient.post()
                    .uri("/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("text", text))
                    .retrieve()
                    .body(AnalysisResult.class);
            if (result == null) {
                throw new InferenceUnavailableException("Empty response from inference service", null);
            }
            return result;
        } catch (RestClientException e) {
            throw new InferenceUnavailableException("Inference service unavailable", e);
        }
    }
}
