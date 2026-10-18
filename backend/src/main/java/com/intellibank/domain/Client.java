package com.intellibank.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clients")
public class Client {
    @Id
    private UUID id;
    @Column(nullable = false, length = 140)
    private String name;
    @Column(nullable = false, unique = true, length = 180)
    private String email;
    @Column(name = "risk_profile", nullable = false, length = 40)
    private String riskProfile;
    @Column(nullable = false, length = 100)
    private String industry;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Client() {}
    public Client(String name, String email, String riskProfile, String industry) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.email = email;
        this.riskProfile = riskProfile;
        this.industry = industry;
        this.createdAt = Instant.now();
    }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRiskProfile() { return riskProfile; }
    public void setRiskProfile(String riskProfile) { this.riskProfile = riskProfile; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public Instant getCreatedAt() { return createdAt; }
}
