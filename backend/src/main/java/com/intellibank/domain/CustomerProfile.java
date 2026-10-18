package com.intellibank.domain;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="customer_profiles")
public class CustomerProfile {
 @Id private UUID userId;
 @Column(nullable=false, length=120) private String fullName;
 @Column(nullable=false, length=25) private String phone;
 @Column(nullable=false) private Instant termsAcceptedAt;
 protected CustomerProfile() {}
 public CustomerProfile(UUID id, String name, String phone) { this.userId=id; this.fullName=name.trim(); this.phone=phone.trim(); this.termsAcceptedAt=Instant.now(); }
 public String getFullName() { return fullName; }
 public String getPhone() { return phone; }
}
