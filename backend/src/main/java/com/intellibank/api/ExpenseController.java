package com.intellibank.api;

import com.intellibank.api.dto.ApiDtos.*;
import com.intellibank.service.BankingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expenses")
public class ExpenseController {
    private final BankingService service;
    public ExpenseController(BankingService service) { this.service = service; }
    @GetMapping public List<LedgerResponse> list() { return service.expenses(); }
    @GetMapping("/{id}") public LedgerResponse get(@PathVariable UUID id) { return service.expense(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public LedgerResponse create(@Valid @RequestBody LedgerRequest request) { return service.createExpense(request); }
    @PutMapping("/{id}") public LedgerResponse update(@PathVariable UUID id, @Valid @RequestBody LedgerRequest request) {
        return service.updateExpense(id, request);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) { service.deleteExpense(id); }
}
