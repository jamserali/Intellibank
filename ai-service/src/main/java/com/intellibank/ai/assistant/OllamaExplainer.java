package com.intellibank.ai.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OllamaExplainer {
    private final boolean enabled;
    private final String model;
    private final RestClient client;

    public OllamaExplainer(
            RestClient.Builder builder,
            @Value("${ollama.enabled:false}") boolean enabled,
            @Value("${ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${ollama.model:llama3.2:3b}") String model) {
        this.enabled = enabled;
        this.model = model;
        this.client = builder.baseUrl(baseUrl).build();
    }

    public String explainConcept(String question) {
        if (!enabled || !isGeneralExplanation(question)) return null;
        JsonNode response = client.post().uri("/api/chat")
                .body(Map.of(
                        "model", model,
                        "stream", false,
                        "messages", List.of(
                                Map.of("role", "system", "content",
                                        "Explain general investment-banking accounting terminology only. Do not make client-specific claims, provide investment advice, or invent financial figures."),
                                Map.of("role", "user", "content", question)))
                )
                .retrieve()
                .body(JsonNode.class);
        String answer = response == null ? "" : response.path("message").path("content").asText("");
        if (answer.isBlank()) throw new IllegalStateException("Ollama returned an empty explanation");
        return answer;
    }

    private boolean isGeneralExplanation(String question) {
        String normalized = question.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("difference between realized and unrealized")
                || normalized.contains("what is realized")
                || normalized.contains("what is unrealized")
                || normalized.contains("explain roi")
                || normalized.contains("what is roi");
    }
}
