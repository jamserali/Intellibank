package com.intellibank.ai.banking;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.intellibank.ai.config.BankingApiProperties;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BankingApiClientTest {

    @Test
    void rejectsUnsafeClientIdWithoutCallingBackend() {
        FakeBankingApiClient client = new FakeBankingApiClient();

        assertThrows(IllegalArgumentException.class,
                () -> client.invoke("get_client_details", Map.of("clientId", "../other")));

        org.junit.jupiter.api.Assertions.assertEquals(0, client.calls);
    }

    @Test
    void rejectsUnknownToolWithoutCallingBackend() {
        FakeBankingApiClient client = new FakeBankingApiClient();

        assertThrows(IllegalArgumentException.class,
                () -> client.invoke("invented_tool", Map.of()));

        org.junit.jupiter.api.Assertions.assertEquals(0, client.calls);
    }

    private static class FakeBankingApiClient extends BankingApiClient {
        private int calls;

        FakeBankingApiClient() {
            super(null, new BankingApiProperties());
        }

        @Override
        public JsonNode get(String path, Map<String, ?> pathVariables, Map<String, ?> query) {
            calls++;
            return null;
        }
    }
}
