package com.intellibank.ai.assistant;

import java.util.Map;

public record AssistantRequest(String tool, Map<String, Object> arguments, String message) {
    public AssistantRequest {
        arguments = arguments == null ? Map.of() : Map.copyOf(arguments);
    }

    public AssistantRequest(String tool, Map<String, Object> arguments) {
        this(tool, arguments, null);
    }
}
