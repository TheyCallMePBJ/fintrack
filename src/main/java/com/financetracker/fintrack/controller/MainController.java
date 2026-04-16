package com.financetracker.fintrack.controller;

import com.financetracker.fintrack.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import com.financetracker.fintrack.model.Transaction;

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

    @GetMapping("/reports")
    public String reports(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        int currentYear = LocalDate.now().getYear();
        LocalDate start = LocalDate.of(currentYear, 1, 1);
        LocalDate end = LocalDate.of(currentYear, 12, 31);

        List<Transaction> expenses = service.getByTypeAndDateRange("expense", start, end);

        Map<String, double[]> categoryData = new HashMap<>();

        for (Transaction t : expenses) {
            String cat = t.getCategory() != null && !t.getCategory().trim().isEmpty() ? t.getCategory() : "Other";
            int monthIndex = t.getDate().getMonthValue() - 1;

            categoryData.putIfAbsent(cat, new double[12]);
            categoryData.get(cat)[monthIndex] += t.getAmount();
        }

        List<String> labels = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

        model.addAttribute("labels", labels);
        model.addAttribute("categoryData", categoryData);
        model.addAttribute("currentYear", currentYear);

        return "reports";
    }

    @GetMapping("/history")
    public String historyPage(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        LocalDate start;
        LocalDate end;
        
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            start = LocalDate.parse(startDate);
            end = LocalDate.parse(endDate);
        } else {
            YearMonth currentMonth = YearMonth.now();
            start = currentMonth.atDay(1);
            end = currentMonth.atEndOfMonth();
        }

        List<Transaction> transactions = service.getByDateRange(start, end);
        model.addAttribute("transactions", transactions);
        model.addAttribute("startDate", start.toString());
        model.addAttribute("endDate", end.toString());

        return "history";
    }
}