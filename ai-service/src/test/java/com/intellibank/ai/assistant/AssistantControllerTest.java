package com.intellibank.ai.assistant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellibank.ai.banking.BankingApiClient;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssistantControllerTest {
    @Mock
    private BankingApiClient banking;
    @Mock
    private OllamaExplainer ollama;

    @Test
    void routesRankingQuestionsToTheBackendAndPreservesCallerAuthorization() throws Exception {
        var controller = new AssistantController(banking, ollama);
        var payload = new ObjectMapper().readTree("[{\"clientName\":\"Fictional Corp\"}]");
        when(banking.invoke("get_top_profitable_clients", Map.<String, Object>of("limit", 5), "Bearer caller-token"))
                .thenReturn(payload);

        AssistantResponse response = controller.chat(
                new AssistantRequest(null, Map.of(), "Show the top 5 profitable clients"),
                "Bearer caller-token");

        assertEquals("get_top_profitable_clients", response.tool());
        assertEquals(payload, response.data());
        assertTrue(response.response().contains("Fictional Corp"));
        verify(banking).invoke("get_top_profitable_clients",
                Map.<String, Object>of("limit", 5), "Bearer caller-token");
    }

    @Test
    void unsupportedQuestionsDoNotCallBankingToolsOrInventFigures() {
        var controller = new AssistantController(banking, ollama);

        AssistantResponse response = controller.chat(
                new AssistantRequest(null, Map.of(), "Tell me a joke"),
                "Bearer caller-token");

        assertEquals("unsupported", response.tool());
        assertEquals("deterministic", response.mode());
        assertNull(response.data());
        verifyNoInteractions(banking);
        verify(ollama).explainConcept("Tell me a joke");
    }
}
