package com.financetracker.fintrack.service;

import com.financetracker.fintrack.model.Transaction;
import com.financetracker.fintrack.repository.TransactionRepository;
import org.springframework.stereotype.Service;

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

    public double getTotalIncome() {
        return repo.findByType("income")
                   .stream()
                   .mapToDouble(Transaction::getAmount)
                   .sum();
    }

    public double getTotalExpense() {
        return repo.findByType("expense")
                   .stream()
                   .mapToDouble(Transaction::getAmount)
                   .sum();
    }
}