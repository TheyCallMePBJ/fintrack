package com.financetracker.fintrack.controller;

import com.financetracker.fintrack.model.Transaction;
import com.financetracker.fintrack.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import jakarta.servlet.http.HttpSession;

@Controller
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @GetMapping("/expense")
    public String expensePage(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        model.addAttribute("recent_expenses", service.getByType("expense"));
        return "expense";
    }

    @PostMapping("/expense")
    public String addExpense(
            @RequestParam String expense_name,
            @RequestParam double expense_amount,
            @RequestParam String expense_category,
            @RequestParam String expense_date
    ) {
        Transaction t = new Transaction();
        t.setDescription(expense_name);
        t.setAmount(expense_amount);
        t.setCategory(expense_category);
        t.setType("expense");
        t.setDate(LocalDate.parse(expense_date));

        service.save(t);
        return "redirect:/expense";
    }

    @GetMapping("/income")
    public String incomePage(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        model.addAttribute("recent_incomes", service.getByType("income"));
        return "income";
    }

    @PostMapping("/income")
    public String addIncome(
            @RequestParam String income_source,
            @RequestParam double income_amount,
            @RequestParam String income_date
    ) {
        Transaction t = new Transaction();
        t.setDescription(income_source);
        t.setAmount(income_amount);
        t.setType("income");
        t.setDate(LocalDate.parse(income_date));

        service.save(t);
        return "redirect:/income";
    }
}