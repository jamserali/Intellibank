package com.intellibank.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "revenues")
public class Revenue {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;
    @Column(nullable = false, length = 180) private String description;
    @Column(nullable = false, length = 80) private String category;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(name = "transaction_date", nullable = false) private LocalDate date;

    protected Revenue() {}
    public Revenue(Client client, String description, String category, BigDecimal amount, LocalDate date) {
        this.id = UUID.randomUUID(); this.client = client; this.description = description;
        this.category = category; this.amount = amount; this.date = date;
    }
    public UUID getId() { return id; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
