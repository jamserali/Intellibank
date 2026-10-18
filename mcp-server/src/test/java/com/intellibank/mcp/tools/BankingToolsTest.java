package com.intellibank.mcp.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellibank.mcp.banking.BankingApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BankingToolsTest {
    private FakeBankingApiClient banking;
    private BankingTools tools;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        banking = new FakeBankingApiClient();
        tools = new BankingTools(banking);
    }

    @Test
    void returnsBackendPayloadAsStructuredData() throws Exception {
        banking.clientDetails = mapper.readTree("{\"id\":\"C-123\"}");

        Map<String, Object> result = tools.getClientDetails("C-123");

        assertEquals("get_client_details", result.get("tool"));
        assertEquals("banking-backend", result.get("source"));
        assertEquals("C-123", ((com.fasterxml.jackson.databind.JsonNode) result.get("data")).path("id").asText());
        assertEquals("C-123", banking.lastClientId);
    }

    @Test
    void rejectsUnsafeClientIdentifiersBeforeCallingBackend() {
        assertThrows(IllegalArgumentException.class, () -> tools.getClientDetails("../admin"));
        assertEquals(0, banking.calls);
    }

    @Test
    void rejectsOutOfRangeLimitsBeforeCallingBackend() {
        assertThrows(IllegalArgumentException.class, () -> tools.getTopProfitableClients(0));
        assertEquals(0, banking.calls);
    }

    private static class FakeBankingApiClient extends BankingApiClient {
        private int calls;
        private String lastClientId;
        private JsonNode clientDetails;

        FakeBankingApiClient() {
            super(null, null);
        }

        @Override
        public JsonNode clientDetails(String clientId) {
            calls++;
            lastClientId = clientId;
            return clientDetails;
        }
    }
}
