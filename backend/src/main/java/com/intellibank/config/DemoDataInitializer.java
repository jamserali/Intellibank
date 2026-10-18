package com.intellibank.config;

import com.intellibank.domain.*;
import com.intellibank.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="intellibank.demo.enabled", havingValue="true", matchIfMissing=true)
@Component
public class DemoDataInitializer implements CommandLineRunner {
    private final ClientRepository clients;
    private final InvestmentRepository investments;
    private final RevenueRepository revenues;
    private final ExpenseRepository expenses;
    private final ValuationRepository valuations;
    private final String[] names = {"Northstar Capital", "Maple Ridge Group", "Cedarstone Partners", "Bluehaven Holdings",
            "Summit & Vale", "Pioneer Financial", "Silverline Ventures", "Meridian Wealth", "Oakwell Investments",
            "Redwood Advisory", "Brightwater Trust", "Evergreen Assets", "Lighthouse Capital", "Westbridge Group",
            "Crownfield Partners"};
    private final String[] industries = {"Technology", "Healthcare", "Energy", "Consumer", "Financial Services"};
    private final String[] sectors = {"Technology", "Healthcare", "Energy", "Industrials", "Consumer", "Financials"};
    private final String[] assets = {"Equity", "Corporate Bond", "Government Bond", "Real Estate Fund", "Index Fund"};

    public DemoDataInitializer(ClientRepository clients, InvestmentRepository investments, RevenueRepository revenues,
                               ExpenseRepository expenses, ValuationRepository valuations) {
        this.clients = clients; this.investments = investments; this.revenues = revenues;
        this.expenses = expenses; this.valuations = valuations;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (clients.count() != 0) return;
        List<Client> clientRows = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            clientRows.add(new Client(names[i], "portfolio" + (i + 1) + "@example.com",
                    new String[]{"Conservative", "Balanced", "Growth"}[i % 3], industries[i % industries.length]));
        }
        clients.saveAll(clientRows);
        List<Investment> investmentRows = new ArrayList<>();
        List<Valuation> valuationRows = new ArrayList<>();
        YearMonth firstMonth = YearMonth.now().minusMonths(11);
        int n = 0;
        for (int clientIndex = 0; clientIndex < clientRows.size(); clientIndex++) {
            for (int item = 0; item < 4; item++) {
                BigDecimal invested = money(18000L + (n * 1725L) + ((n * 137L) % 100));
                BigDecimal current = invested.multiply(new BigDecimal("0.94").add(new BigDecimal("0.018").multiply(BigDecimal.valueOf(n % 11))))
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal realized = money(((n % 7) - 2L) * 83L);
                Investment investment = new Investment(clientRows.get(clientIndex),
                        sectors[n % sectors.length] + " position " + (item + 1), assets[n % assets.length],
                        sectors[n % sectors.length], invested, current, realized, LocalDate.now().minusMonths(18 + n % 20));
                investmentRows.add(investment);
                for (int month = 0; month < 12; month++) {
                    BigDecimal factor = new BigDecimal("0.89").add(new BigDecimal("0.01").multiply(BigDecimal.valueOf(month)))
                            .add(new BigDecimal("0.002").multiply(BigDecimal.valueOf(n % 5)));
                    BigDecimal value = month == 11 ? current : invested.multiply(factor).setScale(2, RoundingMode.HALF_UP);
                    valuationRows.add(new Valuation(investment, firstMonth.plusMonths(month).atDay(1), value));
                }
                n++;
            }
        }
        investments.saveAll(investmentRows);
        valuations.saveAll(valuationRows);

        List<Revenue> revenueRows = new ArrayList<>();
        List<Expense> expenseRows = new ArrayList<>();
        for (int month = 0; month < 12; month++) {
            LocalDate date = firstMonth.plusMonths(month).atDay(12);
            Client assigned = clientRows.get(month % clientRows.size());
            revenueRows.add(new Revenue(null, "Advisory and administration fees", "Service fees",
                    money(24500L + month * 375L), date));
            revenueRows.add(new Revenue(assigned, "Portfolio management fees", "Management fees",
                    money(7800L + month * 125L), date.plusDays(2)));
            expenseRows.add(new Expense(null, "Custody and clearing", "Operations",
                    money(8900L + month * 95L), date.plusDays(1)));
            expenseRows.add(new Expense(assigned, "Client servicing", "Client service",
                    money(2100L + month * 35L), date.plusDays(3)));
        }
        revenues.saveAll(revenueRows);
        expenses.saveAll(expenseRows);
    }

    private BigDecimal money(long value) { return BigDecimal.valueOf(value).setScale(2, RoundingMode.UNNECESSARY); }
}
