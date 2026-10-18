package com.intellibank.mcp.banking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellibank.mcp.config.BankingApiProperties;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class BankingApiClient {
    private final RestClient restClient;
    private final BankingApiProperties properties;
    private volatile String serviceToken;
    private volatile Instant tokenExpiresAt = Instant.EPOCH;

    public BankingApiClient(RestClient bankingRestClient, BankingApiProperties properties) {
        this.restClient = bankingRestClient;
        this.properties = properties;
    }

    public JsonNode get(String path, Map<String, ?> pathVariables, Map<String, ?> query) {
        String uri = UriComponentsBuilder.fromPath(path)
                .queryParams(org.springframework.util.CollectionUtils.isEmpty(query)
                        ? new org.springframework.util.LinkedMultiValueMap<>()
                        : query.entrySet().stream().collect(
                                org.springframework.util.LinkedMultiValueMap::new,
                                (params, entry) -> params.add(entry.getKey(), String.valueOf(entry.getValue())),
                                org.springframework.util.LinkedMultiValueMap::putAll))
                .buildAndExpand(pathVariables)
                .encode()
                .toUriString();
        try {
            var requestSpec = restClient.get().uri(uri);
            String token = serviceToken();
            if (token != null) requestSpec.header("Authorization", "Bearer " + token);
            return requestSpec.retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new BankingApiException(response.getStatusCode().value(),
                                "Banking backend returned HTTP " + response.getStatusCode().value());
                    })
                    .body(JsonNode.class);
        } catch (BankingApiException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BankingApiException("Banking backend request failed or timed out", exception);
        }
    }

    public JsonNode clients() {
        return get(properties.getPaths().getClients(), Map.of(), Map.of());
    }

    public JsonNode clientDetails(String clientId) {
        return get(properties.getPaths().getClientDetails(), Map.of("clientId", clientId), Map.of());
    }

    public JsonNode clientInvestments(String clientId) {
        return get(properties.getPaths().getClientInvestments(), Map.of("clientId", clientId), Map.of());
    }

    public JsonNode clientProfitLoss(String clientId) {
        return get(properties.getPaths().getClientProfitLoss(), Map.of("clientId", clientId), Map.of());
    }

    public JsonNode bankProfitLoss() {
        return get(properties.getPaths().getBankProfitLoss(), Map.of(), Map.of());
    }

    public JsonNode topProfitableClients(int limit) {
        return get(properties.getPaths().getTopProfitableClients(), Map.of(), Map.of("limit", limit));
    }

    public JsonNode bottomProfitableClients(int limit) {
        return get(properties.getPaths().getBottomProfitableClients(), Map.of(), Map.of("limit", limit));
    }

    public JsonNode portfolioSummary() {
        return get(properties.getPaths().getPortfolioSummary(), Map.of(), Map.of());
    }

    public JsonNode revenueSummary() {
        return get(properties.getPaths().getRevenueSummary(), Map.of(), Map.of());
    }

    public JsonNode expenseSummary() {
        return get(properties.getPaths().getExpenseSummary(), Map.of(), Map.of());
    }

    public JsonNode sectorPerformance() {
        return get(properties.getPaths().getSectorPerformance(), Map.of(), Map.of());
    }

    public JsonNode investmentPerformance() {
        return get(properties.getPaths().getInvestmentPerformance(), Map.of(), Map.of());
    }

    public JsonNode compareClientProfitability(String firstClientId, String secondClientId) {
        ObjectNode comparison = JsonNodeFactory.instance.objectNode();
        comparison.set("firstClient", get(properties.getPaths().getClientProfitLoss(),
                Map.of("clientId", firstClientId), Map.of()));
        comparison.set("secondClient", get(properties.getPaths().getClientProfitLoss(),
                Map.of("clientId", secondClientId), Map.of()));
        return comparison;
    }

    public JsonNode profitTrends() {
        return get(properties.getPaths().getProfitTrends(), Map.of(), Map.of());
    }

    private synchronized String serviceToken() {
        if (properties.getUsername().isBlank() || properties.getPassword().isBlank()) return null;
        if (serviceToken != null && tokenExpiresAt.isAfter(Instant.now().plusSeconds(15))) {
            return serviceToken;
        }
        try {
            JsonNode response = restClient.post()
                    .uri("/api/v1/auth/login")
                    .body(Map.of("email", properties.getUsername(), "password", properties.getPassword()))
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null || !response.hasNonNull("accessToken")) {
                throw new BankingApiException("Banking backend login returned no access token", new IllegalStateException());
            }
            serviceToken = response.path("accessToken").asText();
            long expiresIn = Math.max(30, response.path("expiresIn").asLong(3600));
            tokenExpiresAt = Instant.now().plusSeconds(expiresIn);
            return serviceToken;
        } catch (BankingApiException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BankingApiException("Banking backend service authentication failed", exception);
        }
    }
}
