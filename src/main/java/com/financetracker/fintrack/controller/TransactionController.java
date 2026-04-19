package com.financetracker.fintrack.controller;

import com.financetracker.fintrack.model.Transaction;
import com.financetracker.fintrack.service.NlpParserService;
import com.financetracker.fintrack.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import jakarta.servlet.http.HttpSession;

@Controller
public class TransactionController {

    private final TransactionService service;
    private final NlpParserService nlpParser;

    public TransactionController(TransactionService service, NlpParserService nlpParser) {
        this.service = service;
        this.nlpParser = nlpParser;
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

    /**
     * Feature 3: Smart NLP Expense Input
     * Parses a natural-language sentence and pre-fills the expense form.
     */
    @PostMapping("/expense/nlp")
    public String parseNlpExpense(
            @RequestParam String nlpInput,
            HttpSession session
    ) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        Transaction parsed = nlpParser.parse(nlpInput);
        if (parsed == null) {
            // Could not parse — redirect back with error flag
            return "redirect:/expense?nlpError=true";
        }

        // Pre-fill the form via query parameters
        String redirect = "redirect:/expense?nlpAmount=" + parsed.getAmount()
            + "&nlpCategory=" + java.net.URLEncoder.encode(parsed.getCategory(), java.nio.charset.StandardCharsets.UTF_8)
            + "&nlpDate=" + parsed.getDate()
            + "&nlpDescription=" + java.net.URLEncoder.encode(parsed.getDescription(), java.nio.charset.StandardCharsets.UTF_8);
        return redirect;
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