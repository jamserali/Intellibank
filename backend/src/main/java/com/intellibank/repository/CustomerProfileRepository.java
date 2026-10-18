package com.intellibank.repository;
import com.intellibank.domain.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, UUID> {}
