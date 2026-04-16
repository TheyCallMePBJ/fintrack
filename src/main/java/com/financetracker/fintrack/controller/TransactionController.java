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
            @RequestParam(required = false) String description,
            @RequestParam double amount,
            @RequestParam String category,
            @RequestParam String date
    ) {
        Transaction t = new Transaction();
        t.setDescription(description);
        t.setAmount(amount);
        t.setCategory(category);
        t.setType("expense");
        t.setDate(LocalDate.parse(date));

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
            @RequestParam(required = false) String description,
            @RequestParam double amount,
            @RequestParam String category,
            @RequestParam String date
    ) {
        Transaction t = new Transaction();
        t.setDescription(description);
        t.setAmount(amount);
        t.setCategory(category);
        t.setType("income");
        t.setDate(LocalDate.parse(date));

        service.save(t);
        return "redirect:/income";
    }

    @PostMapping("/history/delete/{id}")
    public String deleteTransaction(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        service.delete(id);
        return "redirect:/history";
    }

    @GetMapping("/history/edit/{id}")
    public String editTransactionPage(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        Transaction tx = service.getById(id);
        if (tx == null) {
            return "redirect:/history";
        }
        model.addAttribute("transaction", tx);
        return "edit-transaction";
    }

    @PostMapping("/history/edit/{id}")
    public String editTransaction(
            @PathVariable Long id,
            @RequestParam(required = false) String description,
            @RequestParam double amount,
            @RequestParam String category,
            @RequestParam String date,
            HttpSession session
    ) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        Transaction t = service.getById(id);
        if (t != null) {
            t.setDescription(description);
            t.setAmount(amount);
            t.setCategory(category);
            t.setDate(LocalDate.parse(date));
            service.save(t);
        }
        return "redirect:/history";
    }
}