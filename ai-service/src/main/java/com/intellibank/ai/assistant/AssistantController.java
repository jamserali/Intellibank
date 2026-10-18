package com.intellibank.ai.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.intellibank.ai.banking.BankingApiClient;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {
    private static final Pattern LIMIT = Pattern.compile("\\b(\\d{1,3})\\b");
    private final BankingApiClient banking;
    private final OllamaExplainer ollama;

    public AssistantController(BankingApiClient banking, OllamaExplainer ollama) {
        this.banking = banking;
        this.ollama = ollama;
    }

    @PostMapping("/query")
    public AssistantResponse query(
            @Valid @RequestBody AssistantRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        if (request.tool() == null || request.tool().isBlank()) {
            throw new IllegalArgumentException("tool is required for a direct tool request");
        }
        JsonNode data = banking.invoke(request.tool(), request.arguments(), authorization);
        return response(request.tool(), data, "banking-backend");
    }

    @PostMapping("/chat")
    public AssistantResponse chat(
            @Valid @RequestBody AssistantRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        if (request.tool() != null && !request.tool().isBlank()) {
            JsonNode data = banking.invoke(request.tool(), request.arguments(), authorization);
            return response(request.tool(), data, "banking-backend");
        }
        if (request.message() == null || request.message().isBlank()) {
            throw new IllegalArgumentException("message or tool is required");
        }

        String question = request.message().trim();
        Intent intent = route(question, authorization);
        if (intent == null) {
            String explanation = ollama.explainConcept(question);
            if (explanation != null) {
                return new AssistantResponse("local-ollama", "financial_explanation",
                        "general financial education", explanation, null);
            }
            return new AssistantResponse("deterministic", "unsupported", "none",
                    "I could not map that question to a supported banking-data query. Try asking for bank profit, top/bottom clients, portfolio summary, sector performance, revenue/expenses, profit trends, or a named client's P&L/investments.",
                    null);
        }
        JsonNode data = banking.invoke(intent.tool(), intent.arguments(), authorization);
        return response(intent.tool(), data, "banking-backend");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalidRequest(IllegalArgumentException exception) {
        return Map.of("error", exception.getMessage());
    }

    private AssistantResponse response(String tool, JsonNode data, String source) {
        String text = "Retrieved live data using " + tool + ". The structured result below is returned by the banking API; it is not model-generated.\n\n"
                + data.toPrettyString();
        return new AssistantResponse("deterministic-tool-backed", tool, source, text, data);
    }

    private Intent route(String rawQuestion, String authorization) {
        String question = rawQuestion.toLowerCase(Locale.ROOT);
        if (question.contains("compare") && question.contains("client")) {
            List<JsonNode> matches = matchingClients(question, authorization);
            if (matches.size() == 2) {
                return new Intent("compare_client_profitability", Map.of(
                        "firstClientId", matches.get(0).path("id").asText(),
                        "secondClientId", matches.get(1).path("id").asText()));
            }
        }

        if (question.contains("top") || question.contains("highest profit") || question.contains("most profitable")) {
            return new Intent("get_top_profitable_clients", Map.of("limit", limit(question)));
        }
        if (question.contains("bottom") || question.contains("least profitable") || question.contains("negative")
                || question.contains("loss-making") || question.contains("loss making")) {
            return new Intent("get_bottom_profitable_clients", Map.of("limit", limit(question)));
        }
        if (question.contains("sector") || question.contains("industry")) {
            return new Intent("get_sector_performance", Map.of());
        }
        if (question.contains("portfolio") || question.contains("allocation") || question.contains("exposure")) {
            return new Intent("get_portfolio_summary", Map.of());
        }
        if (question.contains("trend") || question.contains("last six months") || question.contains("historical")) {
            return new Intent("get_profit_trends", Map.of());
        }
        if (question.contains("revenue") || question.contains("income")) {
            return new Intent("get_revenue_summary", Map.of());
        }
        if (question.contains("expense") || question.contains("cost")) {
            return new Intent("get_expense_summary", Map.of());
        }
        if (question.contains("investment") || question.contains("holding")) {
            List<JsonNode> matches = matchingClients(question, authorization);
            if (matches.size() == 1) {
                return new Intent("get_client_investments",
                        Map.of("clientId", matches.get(0).path("id").asText()));
            }
            return new Intent("get_investment_performance", Map.of());
        }
        if (question.contains("profit") || question.contains("p&l") || question.contains("pnl")
                || question.contains("bank performance") || question.contains("total return")) {
            List<JsonNode> matches = matchingClients(question, authorization);
            if (matches.size() == 1) {
                return new Intent("get_client_profit_loss",
                        Map.of("clientId", matches.get(0).path("id").asText()));
            }
            return new Intent("get_bank_profit_loss", Map.of());
        }
        if (question.contains("client")) {
            return new Intent("get_all_clients", Map.of());
        }
        return null;
    }

    private List<JsonNode> matchingClients(String question, String authorization) {
        JsonNode response = banking.invoke("get_all_clients", Map.of(), authorization);
        JsonNode clients = response.isArray() ? response : response.path("items");
        List<JsonNode> matches = new ArrayList<>();
        if (!clients.isArray()) return matches;
        for (JsonNode client : clients) {
            String name = client.path("name").asText("").toLowerCase(Locale.ROOT);
            if (!name.isBlank() && question.contains(name)) matches.add(client);
        }
        return matches;
    }

    private int limit(String question) {
        Matcher matcher = LIMIT.matcher(question);
        if (!matcher.find()) return 5;
        int requested = Integer.parseInt(matcher.group(1));
        return Math.max(1, Math.min(100, requested));
    }

    private record Intent(String tool, Map<String, Object> arguments) {}
}
