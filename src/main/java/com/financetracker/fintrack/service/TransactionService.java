package com.financetracker.fintrack.service;

import com.financetracker.fintrack.model.Transaction;
import com.financetracker.fintrack.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository repo;

    public TransactionService(TransactionRepository repo) {
        this.repo = repo;
    }

    public Transaction save(Transaction t) {
        return repo.save(t);
    }

    public List<Transaction> getAll() {
        return repo.findAll();
    }

    public List<Transaction> getByType(String type) {
        return repo.findByType(type);
    }
    
    public List<Transaction> getByTypeAndDateRange(String type, LocalDate start, LocalDate end) {
        return repo.findByTypeAndDateBetween(type, start, end);
    }
    
    public List<Transaction> getRecentTransactions() {
        return repo.findTop5ByOrderByDateDesc();
    }

    public double getCurrentMonthTotalIncome() {
        YearMonth currentMonth = YearMonth.now();
        LocalDate start = currentMonth.atDay(1);
        LocalDate end = currentMonth.atEndOfMonth();
        return repo.findByTypeAndDateBetween("income", start, end)
                   .stream()
                   .mapToDouble(Transaction::getAmount)
                   .sum();
    }

    public double getCurrentMonthTotalExpense() {
        YearMonth currentMonth = YearMonth.now();
        List<Transaction> expenses = repo.findByTypeAndDateBetween("expense", currentMonth.atDay(1), currentMonth.atEndOfMonth());
        return expenses.stream().mapToDouble(Transaction::getAmount).sum();
    }

    public List<Transaction> getByDateRange(LocalDate start, LocalDate end) {
        return repo.findByDateBetweenOrderByDateDesc(start, end);
    }

    public Transaction getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}