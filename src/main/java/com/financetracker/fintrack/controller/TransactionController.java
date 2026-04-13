package com.financetracker.fintrack.controller;

import com.financetracker.fintrack.model.Transaction;
import com.financetracker.fintrack.model.User;
import com.financetracker.fintrack.repository.TransactionRepository;
import com.financetracker.fintrack.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    // TEMP: using user id = 1 (we'll fix login later)
    private User getDummyUser() {
        return userRepository.findById(1L).orElse(null);
    }

    @GetMapping("/income")
    public String incomePage() {
        return "income";
    }

    @PostMapping("/income")
    public String addIncome(@RequestParam double amount,
                            @RequestParam String description) {

        Transaction t = new Transaction();
        t.setAmount(amount);
        t.setDescription(description);
        t.setType("income");
        t.setUser(getDummyUser());

        transactionRepository.save(t);

        return "redirect:/home";
    }

    @GetMapping("/expense")
    public String expensePage() {
        return "expense";
    }

    @PostMapping("/expense")
    public String addExpense(@RequestParam double amount,
                             @RequestParam String description) {

        Transaction t = new Transaction();
        t.setAmount(amount);
        t.setDescription(description);
        t.setType("expense");
        t.setUser(getDummyUser());

        transactionRepository.save(t);

        return "redirect:/home";
    }
}