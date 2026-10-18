package com.intellibank.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "investments")
public class Investment {
    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    @Column(nullable = false, length = 160)
    private String name;
    @Column(name = "asset_type", nullable = false, length = 80)
    private String assetType;
    @Column(nullable = false, length = 100)
    private String sector;
    @Column(name = "invested_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal investedAmount;
    @Column(name = "current_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentValue;
    @Column(name = "realized_gain", nullable = false, precision = 19, scale = 2)
    private BigDecimal realizedGain;
    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    protected Investment() {}
    public Investment(Client client, String name, String assetType, String sector, BigDecimal investedAmount,
                      BigDecimal currentValue, BigDecimal realizedGain, LocalDate purchaseDate) {
        this.id = UUID.randomUUID();
        this.client = client;
        this.name = name;
        this.assetType = assetType;
        this.sector = sector;
        this.investedAmount = investedAmount;
        this.currentValue = currentValue;
        this.realizedGain = realizedGain;
        this.purchaseDate = purchaseDate;
    }
    public UUID getId() { return id; }
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAssetType() { return assetType; }
    public void setAssetType(String assetType) { this.assetType = assetType; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public BigDecimal getInvestedAmount() { return investedAmount; }
    public void setInvestedAmount(BigDecimal investedAmount) { this.investedAmount = investedAmount; }
    public BigDecimal getCurrentValue() { return currentValue; }
    public void setCurrentValue(BigDecimal currentValue) { this.currentValue = currentValue; }
    public BigDecimal getRealizedGain() { return realizedGain; }
    public void setRealizedGain(BigDecimal realizedGain) { this.realizedGain = realizedGain; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
}
