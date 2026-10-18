package com.intellibank.repository;

import com.intellibank.domain.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByDateBetween(LocalDate from, LocalDate to);
    List<Expense> findByClientIdAndDateBetween(UUID clientId, LocalDate from, LocalDate to);
}
