package com.financetracker.fintrack.repository;

import com.financetracker.fintrack.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByType(String type);

    List<Transaction> findByTypeAndDateBetween(String type, LocalDate startDate, LocalDate endDate);
    
    List<Transaction> findTop5ByOrderByDateDesc();

}