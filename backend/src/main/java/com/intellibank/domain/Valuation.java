package com.intellibank.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "valuations", uniqueConstraints = @UniqueConstraint(columnNames = {"investment_id", "valuation_month"}))
public class Valuation {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investment_id", nullable = false)
    private Investment investment;
    @Column(name = "valuation_month", nullable = false)
    private LocalDate month;
    @Column(name = "valuation_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal value;

    protected Valuation() {}
    public Valuation(Investment investment, LocalDate month, BigDecimal value) {
        this.id = UUID.randomUUID(); this.investment = investment; this.month = month; this.value = value;
    }
    public UUID getId() { return id; }
    public Investment getInvestment() { return investment; }
    public LocalDate getMonth() { return month; }
    public BigDecimal getValue() { return value; }
}
