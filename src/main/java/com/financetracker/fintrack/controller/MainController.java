package com.financetracker.fintrack.controller;

import com.financetracker.fintrack.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

    private final TransactionService service;

    public MainController(TransactionService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {

        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        double income = service.getCurrentMonthTotalIncome();
        double expense = service.getCurrentMonthTotalExpense();

        model.addAttribute("totalIncome", income);
        model.addAttribute("totalExpense", expense);
        model.addAttribute("balance", income - expense);
        model.addAttribute("transactions", service.getRecentTransactions());

        return "index";
    }
}