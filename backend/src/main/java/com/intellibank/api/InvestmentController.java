package com.intellibank.api;

import com.intellibank.api.dto.ApiDtos.*;
import com.intellibank.service.BankingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investments")
public class InvestmentController {
    private final BankingService service;
    public InvestmentController(BankingService service) { this.service = service; }
    @GetMapping public List<InvestmentResponse> list() { return service.investments(); }
    @GetMapping("/client/{clientId}")
    public List<InvestmentResponse> listByClient(@PathVariable UUID clientId) {
        return service.investmentsByClient(clientId);
    }
    @GetMapping("/{id}") public InvestmentResponse get(@PathVariable UUID id) { return service.investment(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public InvestmentResponse create(@Valid @RequestBody InvestmentRequest request) { return service.createInvestment(request); }
    @PutMapping("/{id}") public InvestmentResponse update(@PathVariable UUID id, @Valid @RequestBody InvestmentRequest request) {
        return service.updateInvestment(id, request);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) { service.deleteInvestment(id); }
}
