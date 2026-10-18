package com.intellibank.api;

import com.intellibank.api.dto.ApiDtos.*;
import com.intellibank.service.BankingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ClientController {
    private final BankingService service;
    public ClientController(BankingService service) { this.service = service; }
    @GetMapping("/clients") public List<ClientResponse> list() { return service.clients(); }
    @GetMapping("/clients/{id}") public ClientResponse get(@PathVariable UUID id) { return service.client(id); }
    @PostMapping("/clients") @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse create(@Valid @RequestBody ClientRequest request) { return service.createClient(request); }
    @PutMapping("/clients/{id}") public ClientResponse update(@PathVariable UUID id, @Valid @RequestBody ClientRequest request) {
        return service.updateClient(id, request);
    }
    @DeleteMapping("/clients/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) { service.deleteClient(id); }
    @GetMapping("/clients/{id}/pnl")
    public PnlResponse pnl(@PathVariable UUID id, @RequestParam(required = false) LocalDate from,
                           @RequestParam(required = false) LocalDate to) { return service.clientPnl(id, from, to); }
}
