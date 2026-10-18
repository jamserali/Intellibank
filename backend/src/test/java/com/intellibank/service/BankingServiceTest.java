package com.intellibank.service;

import com.intellibank.domain.*;
import com.intellibank.repository.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BankingServiceTest {
    @Test
    void pnlKeepsAccountingProfitSeparateFromInvestmentReturn() {
        ClientRepository clients = mock(ClientRepository.class);
        InvestmentRepository investments = mock(InvestmentRepository.class);
        RevenueRepository revenues = mock(RevenueRepository.class);
        ExpenseRepository expenses = mock(ExpenseRepository.class);
        ValuationRepository valuations = mock(ValuationRepository.class);
        BankingService service = new BankingService(clients, investments, revenues, expenses, valuations);
        UUID clientId = UUID.randomUUID();
        Client client = new Client("Example Client", "example@example.com", "Balanced", "Technology");
        when(clients.findById(clientId)).thenReturn(Optional.of(client));
        LocalDate from = LocalDate.of(2025, 1, 1), to = LocalDate.of(2025, 12, 31);
        when(revenues.findByClientIdAndDateBetween(clientId, from, to))
                .thenReturn(List.of(new Revenue(client, "Fee", "Service", new BigDecimal("1000.00"), from)));
        when(expenses.findByClientIdAndDateBetween(clientId, from, to))
                .thenReturn(List.of(new Expense(client, "Cost", "Operations", new BigDecimal("250.00"), from)));
        when(investments.findByClientId(clientId)).thenReturn(List.of(new Investment(client, "Equity", "Stock", "Technology",
                new BigDecimal("100.00"), new BigDecimal("125.00"), new BigDecimal("10.00"), from)));

        var result = service.clientPnl(clientId, from, to);

        assertThat(result.accountingProfit()).isEqualByComparingTo("750.00");
        assertThat(result.realizedReturn()).isEqualByComparingTo("10.00");
        assertThat(result.unrealizedReturn()).isEqualByComparingTo("25.00");
        assertThat(result.economicReturn()).isEqualByComparingTo("35.00");
    }
}
