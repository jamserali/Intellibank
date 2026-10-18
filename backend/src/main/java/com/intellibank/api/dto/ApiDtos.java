package com.intellibank.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class ApiDtos {
    private ApiDtos() {}

    public record ClientRequest(@NotBlank @Size(max = 140) String name,
                                @NotBlank @Email @Size(max = 180) String email,
                                @NotBlank @Size(max = 40) String riskProfile,
                                @NotBlank @Size(max = 100) String industry) {}
    public record ClientResponse(UUID id, String name, String email, String riskProfile, String industry, Instant createdAt) {}

    public record InvestmentRequest(@NotNull UUID clientId, @NotBlank @Size(max = 160) String name,
                                    @NotBlank @Size(max = 80) String assetType,
                                    @NotBlank @Size(max = 100) String sector,
                                    @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal investedAmount,
                                    @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal currentValue,
                                    @NotNull @Digits(integer = 17, fraction = 2) BigDecimal realizedGain,
                                    @NotNull LocalDate purchaseDate) {}
    public record InvestmentResponse(UUID id, UUID clientId, String clientName, String name, String assetType,
                                     String sector, BigDecimal investedAmount, BigDecimal currentValue,
                                     BigDecimal realizedGain, BigDecimal unrealizedGain, BigDecimal economicReturn,
                                     LocalDate purchaseDate) {}

    public record LedgerRequest(@NotBlank @Size(max = 180) String description, @NotBlank @Size(max = 80) String category,
                                @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
                                @NotNull LocalDate date, UUID clientId) {}
    public record LedgerResponse(UUID id, UUID clientId, String clientName, String description, String category,
                                 BigDecimal amount, LocalDate date) {}

    public record PnlResponse(String scope, UUID clientId, LocalDate from, LocalDate to,
                              BigDecimal revenue, BigDecimal expense, BigDecimal accountingProfit,
                              BigDecimal realizedReturn, BigDecimal unrealizedReturn, BigDecimal economicReturn) {}
    public record TrendPoint(String month, BigDecimal revenue, BigDecimal expenses,
                             BigDecimal accountingProfit, BigDecimal portfolioValue) {}
    public record DashboardSummary(long clientCount, long investmentCount, BigDecimal totalRevenue,
                                   BigDecimal totalExpenses, BigDecimal accountingProfit,
                                   BigDecimal investedAmount, BigDecimal currentPortfolioValue,
                                   BigDecimal realizedReturn, BigDecimal unrealizedReturn, BigDecimal economicReturn,
                                   BigDecimal totalRoi, long clientsWithHoldings, long profitableClients,
                                   long lossMakingClients) {}
    public record ClientRanking(UUID clientId, String clientName, BigDecimal investedAmount, BigDecimal currentValue,
                                BigDecimal realizedReturn, BigDecimal unrealizedReturn, BigDecimal economicReturn) {}
    public record AssetAllocation(String assetType, long investmentCount, BigDecimal investedAmount,
                                  BigDecimal currentValue, BigDecimal realizedReturn, BigDecimal unrealizedReturn,
                                  BigDecimal economicReturn) {}
    public record PortfolioSummary(long investmentCount, BigDecimal investedAmount, BigDecimal currentValue,
                                   BigDecimal unrealizedReturn, BigDecimal realizedReturn, BigDecimal economicReturn) {}
    public record SectorSummary(String sector, long investmentCount, BigDecimal investedAmount,
                                BigDecimal currentValue, BigDecimal realizedReturn, BigDecimal unrealizedReturn,
                                BigDecimal economicReturn) {}
}
