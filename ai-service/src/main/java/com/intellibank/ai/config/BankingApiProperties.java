package com.intellibank.ai.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "banking.api")
public class BankingApiProperties {
    private String baseUrl = "http://localhost:8080";
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(10);
    private final Paths paths = new Paths();

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String value) { baseUrl = value; }
    public Duration getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(Duration value) { connectTimeout = value; }
    public Duration getReadTimeout() { return readTimeout; }
    public void setReadTimeout(Duration value) { readTimeout = value; }
    public Paths getPaths() { return paths; }

    public static class Paths {
        private String clients = "/api/v1/clients";
        private String clientDetails = "/api/v1/clients/{clientId}";
        private String clientInvestments = "/api/v1/investments/client/{clientId}";
        private String clientProfitLoss = "/api/v1/clients/{clientId}/pnl";
        private String bankProfitLoss = "/api/v1/reports/bank/pnl";
        private String topProfitableClients = "/api/v1/analytics/top-clients";
        private String bottomProfitableClients = "/api/v1/analytics/bottom-clients";
        private String portfolioSummary = "/api/v1/analytics/portfolio";
        private String revenueSummary = "/api/v1/revenues";
        private String expenseSummary = "/api/v1/expenses";
        private String sectorPerformance = "/api/v1/analytics/sectors";
        private String investmentPerformance = "/api/v1/investments";
        private String compareClientProfitability = "/api/v1/clients/{clientId}/pnl";
        private String profitTrends = "/api/v1/dashboard/trends";

        public String getClients() { return clients; }
        public void setClients(String value) { clients = value; }
        public String getClientDetails() { return clientDetails; }
        public void setClientDetails(String value) { clientDetails = value; }
        public String getClientInvestments() { return clientInvestments; }
        public void setClientInvestments(String value) { clientInvestments = value; }
        public String getClientProfitLoss() { return clientProfitLoss; }
        public void setClientProfitLoss(String value) { clientProfitLoss = value; }
        public String getBankProfitLoss() { return bankProfitLoss; }
        public void setBankProfitLoss(String value) { bankProfitLoss = value; }
        public String getTopProfitableClients() { return topProfitableClients; }
        public void setTopProfitableClients(String value) { topProfitableClients = value; }
        public String getBottomProfitableClients() { return bottomProfitableClients; }
        public void setBottomProfitableClients(String value) { bottomProfitableClients = value; }
        public String getPortfolioSummary() { return portfolioSummary; }
        public void setPortfolioSummary(String value) { portfolioSummary = value; }
        public String getRevenueSummary() { return revenueSummary; }
        public void setRevenueSummary(String value) { revenueSummary = value; }
        public String getExpenseSummary() { return expenseSummary; }
        public void setExpenseSummary(String value) { expenseSummary = value; }
        public String getSectorPerformance() { return sectorPerformance; }
        public void setSectorPerformance(String value) { sectorPerformance = value; }
        public String getInvestmentPerformance() { return investmentPerformance; }
        public void setInvestmentPerformance(String value) { investmentPerformance = value; }
        public String getCompareClientProfitability() { return compareClientProfitability; }
        public void setCompareClientProfitability(String value) { compareClientProfitability = value; }
        public String getProfitTrends() { return profitTrends; }
        public void setProfitTrends(String value) { profitTrends = value; }
    }
}
