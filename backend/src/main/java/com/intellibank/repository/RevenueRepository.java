package com.intellibank.repository;

import com.intellibank.domain.Revenue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RevenueRepository extends JpaRepository<Revenue, UUID> {
    List<Revenue> findByDateBetween(LocalDate from, LocalDate to);
    List<Revenue> findByClientIdAndDateBetween(UUID clientId, LocalDate from, LocalDate to);
    List<Revenue> findByDateLessThanEqual(LocalDate to);
}
