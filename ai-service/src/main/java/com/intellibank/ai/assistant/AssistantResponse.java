package com.intellibank.ai.assistant;

import com.fasterxml.jackson.databind.JsonNode;

public record AssistantResponse(String mode, String tool, String source, String response, JsonNode data) {
    public AssistantResponse(String mode, String tool, String source, JsonNode data) {
        this(mode, tool, source, "", data);
    }
}
