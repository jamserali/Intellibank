package com.intellibank.ai.banking;

import com.fasterxml.jackson.databind.JsonNode;
import com.intellibank.ai.config.BankingApiProperties;
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

    public BankingApiClient(RestClient bankingRestClient, BankingApiProperties properties) {
        restClient = bankingRestClient;
        this.properties = properties;
    }

    public JsonNode get(String path, Map<String, ?> pathVariables, Map<String, ?> query) {
        return get(path, pathVariables, query, null);
    }

    public JsonNode get(String path, Map<String, ?> pathVariables, Map<String, ?> query, String authorization) {
        String uri = UriComponentsBuilder.fromPath(path)
                .queryParams(query.entrySet().stream().collect(
                        org.springframework.util.LinkedMultiValueMap::new,
                        (params, entry) -> params.add(entry.getKey(), String.valueOf(entry.getValue())),
                        org.springframework.util.LinkedMultiValueMap::putAll))
                .buildAndExpand(pathVariables)
                .encode()
                .toUriString();
        try {
            var requestSpec = restClient.get().uri(uri);
            if (authorization != null && authorization.startsWith("Bearer ")) {
                requestSpec.header("Authorization", authorization);
            }
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

    public JsonNode invoke(String tool, Map<String, Object> args) {
        return invoke(tool, args, null);
    }

    public JsonNode invoke(String tool, Map<String, Object> args, String authorization) {
        return switch (tool) {
            case "get_all_clients" -> get(properties.getPaths().getClients(), Map.of(), Map.of(), authorization);
            case "get_client_details" -> client(properties.getPaths().getClientDetails(), args, authorization);
            case "get_client_investments" -> client(properties.getPaths().getClientInvestments(), args, authorization);
            case "get_client_profit_loss" -> client(properties.getPaths().getClientProfitLoss(), args, authorization);
            case "get_bank_profit_loss" -> get(properties.getPaths().getBankProfitLoss(), Map.of(), Map.of(), authorization);
            case "get_top_profitable_clients" -> ranking(properties.getPaths().getTopProfitableClients(), args, authorization);
            case "get_bottom_profitable_clients" -> ranking(properties.getPaths().getBottomProfitableClients(), args, authorization);
            case "get_portfolio_summary" -> get(properties.getPaths().getPortfolioSummary(), Map.of(), Map.of(), authorization);
            case "get_revenue_summary" -> get(properties.getPaths().getRevenueSummary(), Map.of(), Map.of(), authorization);
            case "get_expense_summary" -> get(properties.getPaths().getExpenseSummary(), Map.of(), Map.of(), authorization);
            case "get_sector_performance" -> get(properties.getPaths().getSectorPerformance(), Map.of(), Map.of(), authorization);
            case "get_investment_performance" -> get(properties.getPaths().getInvestmentPerformance(), Map.of(), Map.of(), authorization);
            case "compare_client_profitability" -> compare(args, authorization);
            case "get_profit_trends" -> get(properties.getPaths().getProfitTrends(), Map.of(), Map.of("months", 12), authorization);
            default -> throw new IllegalArgumentException("Unsupported tool: " + tool);
        };
    }

    private JsonNode client(String path, Map<String, Object> args, String authorization) {
        String clientId = clientId(requiredString(args, "clientId"));
        return get(path, Map.of("clientId", clientId), Map.of(), authorization);
    }

    private JsonNode ranking(String path, Map<String, Object> args, String authorization) {
        Object raw = args.getOrDefault("limit", 10);
        if (!(raw instanceof Number number) || number.intValue() < 1 || number.intValue() > 100
                || number.doubleValue() != number.intValue()) {
            throw new IllegalArgumentException("limit must be an integer from 1 to 100");
        }
        return get(path, Map.of(), Map.of("limit", number.intValue()), authorization);
    }

    private JsonNode compare(Map<String, Object> args, String authorization) {
        String first = clientId(requiredString(args, "firstClientId"));
        String second = clientId(requiredString(args, "secondClientId"));
        if (first.equals(second)) {
            throw new IllegalArgumentException("Client identifiers must be different");
        }
        JsonNode firstResult = get(properties.getPaths().getClientProfitLoss(),
                Map.of("clientId", first), Map.of(), authorization);
        JsonNode secondResult = get(properties.getPaths().getClientProfitLoss(),
                Map.of("clientId", second), Map.of(), authorization);
        var comparison = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        comparison.set("firstClient", firstResult);
        comparison.set("secondClient", secondResult);
        return comparison;
    }

    private String requiredString(Map<String, Object> args, String name) {
        Object value = args.get(name);
        if (!(value instanceof String text) || text.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return text;
    }

    private String clientId(String value) {
        if (!value.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,63}")) {
            throw new IllegalArgumentException("clientId must be 1-64 safe identifier characters");
        }
        return value;
    }
}
