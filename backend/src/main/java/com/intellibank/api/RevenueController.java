package com.intellibank.api;

import com.intellibank.api.dto.ApiDtos.*;
import com.intellibank.service.BankingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/revenues")
public class RevenueController {
    private final BankingService service;
    public RevenueController(BankingService service) { this.service = service; }
    @GetMapping public List<LedgerResponse> list() { return service.revenues(); }
    @GetMapping("/{id}") public LedgerResponse get(@PathVariable UUID id) { return service.revenue(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public LedgerResponse create(@Valid @RequestBody LedgerRequest request) { return service.createRevenue(request); }
    @PutMapping("/{id}") public LedgerResponse update(@PathVariable UUID id, @Valid @RequestBody LedgerRequest request) {
        return service.updateRevenue(id, request);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) { service.deleteRevenue(id); }
}
