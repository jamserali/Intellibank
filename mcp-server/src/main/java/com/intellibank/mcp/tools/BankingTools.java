package com.intellibank.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.intellibank.mcp.banking.BankingApiClient;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class BankingTools {
    private static final Pattern CLIENT_ID = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,63}");
    private final BankingApiClient banking;

    public BankingTools(BankingApiClient banking) {
        this.banking = banking;
    }

    @Tool(name = "get_all_clients", description = "Get all client records from the banking backend.")
    public Map<String, Object> getAllClients() {
        return result("get_all_clients", banking.clients());
    }

    @Tool(name = "get_client_details", description = "Get one client's details by exact client ID.")
    public Map<String, Object> getClientDetails(
            @ToolParam(description = "Exact client identifier") String clientId) {
        return result("get_client_details", banking.clientDetails(validateClientId(clientId)));
    }

    @Tool(name = "get_client_investments", description = "Get investments held by one client.")
    public Map<String, Object> getClientInvestments(
            @ToolParam(description = "Exact client identifier") String clientId) {
        return result("get_client_investments", banking.clientInvestments(validateClientId(clientId)));
    }

    @Tool(name = "get_client_profit_loss", description = "Get backend-reported profit and loss for one client.")
    public Map<String, Object> getClientProfitLoss(
            @ToolParam(description = "Exact client identifier") String clientId) {
        return result("get_client_profit_loss", banking.clientProfitLoss(validateClientId(clientId)));
    }

    @Tool(name = "get_bank_profit_loss", description = "Get bank-wide profit and loss from the backend.")
    public Map<String, Object> getBankProfitLoss() {
        return result("get_bank_profit_loss", banking.bankProfitLoss());
    }

    @Tool(name = "get_top_profitable_clients", description = "Get the backend-ranked most profitable clients.")
    public Map<String, Object> getTopProfitableClients(
            @ToolParam(description = "Maximum result count, from 1 to 100") int limit) {
        return result("get_top_profitable_clients", banking.topProfitableClients(validateLimit(limit)));
    }

    @Tool(name = "get_bottom_profitable_clients", description = "Get the backend-ranked least profitable clients.")
    public Map<String, Object> getBottomProfitableClients(
            @ToolParam(description = "Maximum result count, from 1 to 100") int limit) {
        return result("get_bottom_profitable_clients", banking.bottomProfitableClients(validateLimit(limit)));
    }

    @Tool(name = "get_portfolio_summary", description = "Get the portfolio summary returned by the backend.")
    public Map<String, Object> getPortfolioSummary() {
        return result("get_portfolio_summary", banking.portfolioSummary());
    }

    @Tool(name = "get_revenue_summary", description = "Get the revenue summary returned by the backend.")
    public Map<String, Object> getRevenueSummary() {
        return result("get_revenue_summary", banking.revenueSummary());
    }

    @Tool(name = "get_expense_summary", description = "Get the expense summary returned by the backend.")
    public Map<String, Object> getExpenseSummary() {
        return result("get_expense_summary", banking.expenseSummary());
    }

    @Tool(name = "get_sector_performance", description = "Get backend-reported performance grouped by sector.")
    public Map<String, Object> getSectorPerformance() {
        return result("get_sector_performance", banking.sectorPerformance());
    }

    @Tool(name = "get_investment_performance", description = "Get backend-reported investment performance.")
    public Map<String, Object> getInvestmentPerformance() {
        return result("get_investment_performance", banking.investmentPerformance());
    }

    @Tool(name = "compare_client_profitability", description = "Get the backend comparison for exactly two clients.")
    public Map<String, Object> compareClientProfitability(
            @ToolParam(description = "First exact client identifier") String firstClientId,
            @ToolParam(description = "Second exact client identifier") String secondClientId) {
        String first = validateClientId(firstClientId);
        String second = validateClientId(secondClientId);
        if (first.equals(second)) {
            throw new IllegalArgumentException("Client identifiers must be different");
        }
        return result("compare_client_profitability", banking.compareClientProfitability(first, second));
    }

    @Tool(name = "get_profit_trends", description = "Get profit trends returned by the backend.")
    public Map<String, Object> getProfitTrends() {
        return result("get_profit_trends", banking.profitTrends());
    }

    static String validateClientId(String clientId) {
        if (clientId == null || !CLIENT_ID.matcher(clientId).matches()) {
            throw new IllegalArgumentException("clientId must be 1-64 safe identifier characters");
        }
        return clientId;
    }

    static int validateLimit(int limit) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("limit must be between 1 and 100");
        }
        return limit;
    }

    private Map<String, Object> result(String tool, JsonNode data) {
        if (data == null) {
            throw new IllegalStateException("Banking backend returned an empty response");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("tool", tool);
        result.put("source", "banking-backend");
        result.put("data", data);
        return result;
    }
}
