package com.intellibank.service;

import com.intellibank.api.NotFoundException;
import com.intellibank.api.dto.ApiDtos.*;
import com.intellibank.domain.*;
import com.intellibank.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class BankingService {
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private final ClientRepository clients;
    private final InvestmentRepository investments;
    private final RevenueRepository revenues;
    private final ExpenseRepository expenses;
    private final ValuationRepository valuations;

    public BankingService(ClientRepository clients, InvestmentRepository investments, RevenueRepository revenues,
                          ExpenseRepository expenses, ValuationRepository valuations) {
        this.clients = clients;
        this.investments = investments;
        this.revenues = revenues;
        this.expenses = expenses;
        this.valuations = valuations;
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> clients() { return clients.findAll().stream().map(this::clientDto).toList(); }
    @Transactional(readOnly = true)
    public ClientResponse client(UUID id) { return clientDto(requireClient(id)); }
    public ClientResponse createClient(ClientRequest r) {
        if (clients.existsByEmailIgnoreCase(r.email())) throw new IllegalArgumentException("Email is already registered");
        return clientDto(clients.save(new Client(r.name().trim(), r.email().trim(), r.riskProfile().trim(), r.industry().trim())));
    }
    public ClientResponse updateClient(UUID id, ClientRequest r) {
        Client c = requireClient(id);
        if (clients.existsByEmailIgnoreCaseAndIdNot(r.email(), id)) throw new IllegalArgumentException("Email is already registered");
        c.setName(r.name().trim()); c.setEmail(r.email().trim()); c.setRiskProfile(r.riskProfile().trim()); c.setIndustry(r.industry().trim());
        return clientDto(clients.save(c));
    }
    public void deleteClient(UUID id) { clients.delete(requireClient(id)); }

    @Transactional(readOnly = true)
    public List<InvestmentResponse> investments() { return investments.findAll().stream().map(this::investmentDto).toList(); }
    @Transactional(readOnly = true)
    public List<InvestmentResponse> investmentsByClient(UUID clientId) {
        requireClient(clientId);
        return investments.findByClientId(clientId).stream().map(this::investmentDto).toList();
    }
    @Transactional(readOnly = true)
    public InvestmentResponse investment(UUID id) { return investmentDto(requireInvestment(id)); }
    public InvestmentResponse createInvestment(InvestmentRequest r) {
        return investmentDto(investments.save(new Investment(requireClient(r.clientId()), r.name().trim(), r.assetType().trim(),
                r.sector().trim(), r.investedAmount(), r.currentValue(), r.realizedGain(), r.purchaseDate())));
    }
    public InvestmentResponse updateInvestment(UUID id, InvestmentRequest r) {
        Investment i = requireInvestment(id);
        i.setClient(requireClient(r.clientId())); i.setName(r.name().trim()); i.setAssetType(r.assetType().trim());
        i.setSector(r.sector().trim()); i.setInvestedAmount(r.investedAmount()); i.setCurrentValue(r.currentValue());
        i.setRealizedGain(r.realizedGain()); i.setPurchaseDate(r.purchaseDate());
        return investmentDto(investments.save(i));
    }
    public void deleteInvestment(UUID id) { investments.delete(requireInvestment(id)); }

    @Transactional(readOnly = true)
    public List<LedgerResponse> revenues() { return revenues.findAll().stream().map(this::revenueDto).toList(); }
    @Transactional(readOnly = true)
    public LedgerResponse revenue(UUID id) { return revenueDto(requireRevenue(id)); }
    public LedgerResponse createRevenue(LedgerRequest r) {
        return revenueDto(revenues.save(new Revenue(optionalClient(r.clientId()), r.description().trim(), r.category().trim(), r.amount(), r.date())));
    }
    public LedgerResponse updateRevenue(UUID id, LedgerRequest r) {
        Revenue e = requireRevenue(id); e.setClient(optionalClient(r.clientId())); e.setDescription(r.description().trim());
        e.setCategory(r.category().trim()); e.setAmount(r.amount()); e.setDate(r.date());
        return revenueDto(revenues.save(e));
    }
    public void deleteRevenue(UUID id) { revenues.delete(requireRevenue(id)); }

    @Transactional(readOnly = true)
    public List<LedgerResponse> expenses() { return expenses.findAll().stream().map(this::expenseDto).toList(); }
    @Transactional(readOnly = true)
    public LedgerResponse expense(UUID id) { return expenseDto(requireExpense(id)); }
    public LedgerResponse createExpense(LedgerRequest r) {
        return expenseDto(expenses.save(new Expense(optionalClient(r.clientId()), r.description().trim(), r.category().trim(), r.amount(), r.date())));
    }
    public LedgerResponse updateExpense(UUID id, LedgerRequest r) {
        Expense e = requireExpense(id); e.setClient(optionalClient(r.clientId())); e.setDescription(r.description().trim());
        e.setCategory(r.category().trim()); e.setAmount(r.amount()); e.setDate(r.date());
        return expenseDto(expenses.save(e));
    }
    public void deleteExpense(UUID id) { expenses.delete(requireExpense(id)); }

    @Transactional(readOnly = true)
    public PnlResponse bankPnl(LocalDate from, LocalDate to) {
        DateRange range = dateRange(from, to);
        BigDecimal revenue = revenues.findByDateBetween(range.from, range.to).stream().map(Revenue::getAmount).reduce(ZERO, BigDecimal::add);
        BigDecimal expense = expenses.findByDateBetween(range.from, range.to).stream().map(Expense::getAmount).reduce(ZERO, BigDecimal::add);
        List<Investment> all = investments.findAll();
        BigDecimal realized = sum(all, Investment::getRealizedGain);
        BigDecimal unrealized = all.stream().map(i -> i.getCurrentValue().subtract(i.getInvestedAmount())).reduce(ZERO, BigDecimal::add);
        return pnl("bank", null, range, revenue, expense, realized, unrealized);
    }

    @Transactional(readOnly = true)
    public PnlResponse clientPnl(UUID id, LocalDate from, LocalDate to) {
        requireClient(id);
        DateRange range = dateRange(from, to);
        BigDecimal revenue = revenues.findByClientIdAndDateBetween(id, range.from, range.to).stream().map(Revenue::getAmount).reduce(ZERO, BigDecimal::add);
        BigDecimal expense = expenses.findByClientIdAndDateBetween(id, range.from, range.to).stream().map(Expense::getAmount).reduce(ZERO, BigDecimal::add);
        List<Investment> holdings = investments.findByClientId(id);
        BigDecimal realized = sum(holdings, Investment::getRealizedGain);
        BigDecimal unrealized = holdings.stream().map(i -> i.getCurrentValue().subtract(i.getInvestedAmount())).reduce(ZERO, BigDecimal::add);
        return pnl("client", id, range, revenue, expense, realized, unrealized);
    }

    @Transactional(readOnly = true)
    public DashboardSummary dashboardSummary() {
        List<Investment> all = investments.findAll();
        BigDecimal revenue = revenues.findAll().stream().map(Revenue::getAmount).reduce(ZERO, BigDecimal::add);
        BigDecimal expense = expenses.findAll().stream().map(Expense::getAmount).reduce(ZERO, BigDecimal::add);
        BigDecimal invested = sum(all, Investment::getInvestedAmount);
        BigDecimal current = sum(all, Investment::getCurrentValue);
        BigDecimal realized = sum(all, Investment::getRealizedGain);
        BigDecimal unrealized = current.subtract(invested);
        Map<UUID, List<Investment>> holdingsByClient = all.stream()
                .collect(Collectors.groupingBy(investment -> investment.getClient().getId()));
        List<ClientRanking> rankings = clients.findAll().stream()
                .map(client -> ranking(client, holdingsByClient.getOrDefault(client.getId(), List.of())))
                .toList();
        BigDecimal economic = realized.add(unrealized);
        BigDecimal roi = invested.signum() == 0 ? ZERO
                : economic.multiply(BigDecimal.valueOf(100)).divide(invested, 2, RoundingMode.HALF_UP);
        return new DashboardSummary(clients.count(), all.size(), revenue, expense, revenue.subtract(expense), invested, current,
                realized, unrealized, economic, roi,
                holdingsByClient.size(),
                rankings.stream().filter(client -> client.economicReturn().signum() > 0).count(),
                rankings.stream().filter(client -> client.economicReturn().signum() < 0).count());
    }

    @Transactional(readOnly = true)
    public List<TrendPoint> trends(int months) {
        if (months < 1 || months > 60) throw new IllegalArgumentException("months must be between 1 and 60");
        YearMonth last = YearMonth.now();
        YearMonth first = last.minusMonths(months - 1L);
        LocalDate from = first.atDay(1), to = last.atEndOfMonth();
        Map<YearMonth, BigDecimal> monthlyRevenue = revenues.findByDateBetween(from, to).stream()
                .collect(Collectors.groupingBy(r -> YearMonth.from(r.getDate()), Collectors.reducing(ZERO, Revenue::getAmount, BigDecimal::add)));
        Map<YearMonth, BigDecimal> monthlyExpense = expenses.findByDateBetween(from, to).stream()
                .collect(Collectors.groupingBy(e -> YearMonth.from(e.getDate()), Collectors.reducing(ZERO, Expense::getAmount, BigDecimal::add)));
        Map<YearMonth, BigDecimal> monthlyPortfolio = valuations.findByMonthBetween(from, to).stream()
                .collect(Collectors.groupingBy(v -> YearMonth.from(v.getMonth()), Collectors.reducing(ZERO, Valuation::getValue, BigDecimal::add)));
        List<TrendPoint> result = new ArrayList<>();
        for (YearMonth month = first; !month.isAfter(last); month = month.plusMonths(1)) {
            BigDecimal r = monthlyRevenue.getOrDefault(month, ZERO), e = monthlyExpense.getOrDefault(month, ZERO);
            result.add(new TrendPoint(month.toString(), r, e, r.subtract(e), monthlyPortfolio.getOrDefault(month, ZERO)));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<ClientRanking> clientRankings(int limit, boolean top) {
        validateLimit(limit);
        Comparator<ClientRanking> comparator = Comparator.comparing(ClientRanking::economicReturn).thenComparing(ClientRanking::clientName);
        return clients.findAll().stream()
                .map(c -> ranking(c, investments.findByClientId(c.getId())))
                .sorted(top ? comparator.reversed() : comparator).limit(limit).toList();
    }

    @Transactional(readOnly = true)
    public PortfolioSummary portfolio() {
        List<Investment> all = investments.findAll();
        BigDecimal invested = sum(all, Investment::getInvestedAmount), current = sum(all, Investment::getCurrentValue);
        BigDecimal realized = sum(all, Investment::getRealizedGain), unrealized = current.subtract(invested);
        return new PortfolioSummary(all.size(), invested, current, unrealized, realized, unrealized.add(realized));
    }

    @Transactional(readOnly = true)
    public List<AssetAllocation> assetAllocation() {
        Map<String, List<Investment>> grouped = investments.findAll().stream()
                .collect(Collectors.groupingBy(Investment::getAssetType, TreeMap::new, Collectors.toList()));
        return grouped.entrySet().stream().map(entry -> {
            List<Investment> group = entry.getValue();
            BigDecimal invested = sum(group, Investment::getInvestedAmount);
            BigDecimal current = sum(group, Investment::getCurrentValue);
            BigDecimal realized = sum(group, Investment::getRealizedGain);
            BigDecimal unrealized = current.subtract(invested);
            return new AssetAllocation(entry.getKey(), group.size(), invested, current, realized, unrealized,
                    realized.add(unrealized));
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<SectorSummary> sectors() {
        Map<String, List<Investment>> grouped = investments.findAll().stream().collect(Collectors.groupingBy(Investment::getSector, TreeMap::new, Collectors.toList()));
        return grouped.entrySet().stream().map(entry -> {
            List<Investment> group = entry.getValue();
            BigDecimal invested = sum(group, Investment::getInvestedAmount), current = sum(group, Investment::getCurrentValue);
            BigDecimal realized = sum(group, Investment::getRealizedGain), unrealized = current.subtract(invested);
            return new SectorSummary(entry.getKey(), group.size(), invested, current, realized, unrealized, realized.add(unrealized));
        }).toList();
    }

    private PnlResponse pnl(String scope, UUID clientId, DateRange range, BigDecimal revenue, BigDecimal expense,
                            BigDecimal realized, BigDecimal unrealized) {
        return new PnlResponse(scope, clientId, range.from, range.to, revenue, expense, revenue.subtract(expense),
                realized, unrealized, realized.add(unrealized));
    }
    private ClientRanking ranking(Client c, List<Investment> holdings) {
        BigDecimal invested = sum(holdings, Investment::getInvestedAmount), current = sum(holdings, Investment::getCurrentValue);
        BigDecimal realized = sum(holdings, Investment::getRealizedGain), unrealized = current.subtract(invested);
        return new ClientRanking(c.getId(), c.getName(), invested, current, realized, unrealized, realized.add(unrealized));
    }
    private <T> BigDecimal sum(List<T> items, Function<T, BigDecimal> value) {
        return items.stream().map(value).reduce(ZERO, BigDecimal::add);
    }
    private DateRange dateRange(LocalDate from, LocalDate to) {
        LocalDate end = to == null ? LocalDate.now() : to;
        LocalDate start = from == null ? end.withDayOfYear(1) : from;
        if (start.isAfter(end)) throw new IllegalArgumentException("'from' must not be after 'to'");
        return new DateRange(start, end);
    }
    private void validateLimit(int limit) {
        if (limit < 1 || limit > 100) throw new IllegalArgumentException("limit must be between 1 and 100");
    }
    private Client requireClient(UUID id) { return clients.findById(id).orElseThrow(() -> new NotFoundException("Client not found: " + id)); }
    private Client optionalClient(UUID id) { return id == null ? null : requireClient(id); }
    private Investment requireInvestment(UUID id) { return investments.findById(id).orElseThrow(() -> new NotFoundException("Investment not found: " + id)); }
    private Revenue requireRevenue(UUID id) { return revenues.findById(id).orElseThrow(() -> new NotFoundException("Revenue not found: " + id)); }
    private Expense requireExpense(UUID id) { return expenses.findById(id).orElseThrow(() -> new NotFoundException("Expense not found: " + id)); }
    private ClientResponse clientDto(Client c) { return new ClientResponse(c.getId(), c.getName(), c.getEmail(), c.getRiskProfile(), c.getIndustry(), c.getCreatedAt()); }
    private InvestmentResponse investmentDto(Investment i) {
        BigDecimal unrealized = i.getCurrentValue().subtract(i.getInvestedAmount());
        return new InvestmentResponse(i.getId(), i.getClient().getId(), i.getClient().getName(), i.getName(), i.getAssetType(),
                i.getSector(), i.getInvestedAmount(), i.getCurrentValue(), i.getRealizedGain(), unrealized,
                i.getRealizedGain().add(unrealized), i.getPurchaseDate());
    }
    private LedgerResponse revenueDto(Revenue r) {
        return new LedgerResponse(r.getId(), r.getClient() == null ? null : r.getClient().getId(),
                r.getClient() == null ? null : r.getClient().getName(), r.getDescription(), r.getCategory(), r.getAmount(), r.getDate());
    }
    private LedgerResponse expenseDto(Expense e) {
        return new LedgerResponse(e.getId(), e.getClient() == null ? null : e.getClient().getId(),
                e.getClient() == null ? null : e.getClient().getName(), e.getDescription(), e.getCategory(), e.getAmount(), e.getDate());
    }
    private record DateRange(LocalDate from, LocalDate to) {}
}
