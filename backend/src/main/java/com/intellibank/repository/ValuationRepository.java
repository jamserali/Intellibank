package com.intellibank.repository;

import com.intellibank.domain.Valuation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ValuationRepository extends JpaRepository<Valuation, UUID> {
    List<Valuation> findByMonthBetween(LocalDate from, LocalDate to);
}
