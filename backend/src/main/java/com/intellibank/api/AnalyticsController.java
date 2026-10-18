package com.intellibank.api;

import com.intellibank.api.dto.ApiDtos.*;
import com.intellibank.service.BankingService;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AnalyticsController {
    private final BankingService service;
    public AnalyticsController(BankingService service) { this.service = service; }
    @GetMapping("/reports/bank/pnl")
    public PnlResponse bankPnl(@RequestParam(required = false) LocalDate from, @RequestParam(required = false) LocalDate to) {
        return service.bankPnl(from, to);
    }
    @GetMapping("/dashboard/summary") public DashboardSummary summary() { return service.dashboardSummary(); }
    @GetMapping("/dashboard/trends") public List<TrendPoint> trends(@RequestParam(defaultValue = "12") int months) {
        return service.trends(months);
    }
    @GetMapping("/analytics/top-clients") public List<ClientRanking> top(@RequestParam(defaultValue = "5") int limit) {
        return service.clientRankings(limit, true);
    }
    @GetMapping("/analytics/bottom-clients") public List<ClientRanking> bottom(@RequestParam(defaultValue = "5") int limit) {
        return service.clientRankings(limit, false);
    }
    @GetMapping("/analytics/portfolio") public PortfolioSummary portfolio() { return service.portfolio(); }
    @GetMapping("/analytics/allocation") public List<AssetAllocation> allocation() { return service.assetAllocation(); }
    @GetMapping("/analytics/sectors") public List<SectorSummary> sectors() { return service.sectors(); }
    @GetMapping("/health") public HealthResponse health() { return new HealthResponse("UP"); }
    public record HealthResponse(String status) {}
}
