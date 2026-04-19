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

    @GetMapping("/transaction")
    public String transactionPage(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        return "add-transaction";
    }

    @PostMapping("/transaction")
    public String addTransaction(
            @RequestParam(required = false) String description,
            @RequestParam double amount,
            @RequestParam String category,
            @RequestParam String date,
            @RequestParam String type
    ) {
        Transaction t = new Transaction();
        t.setDescription(description);
        t.setAmount(amount);
        t.setCategory(category);
        t.setType(type);
        t.setDate(LocalDate.parse(date));

        service.save(t);
        return "redirect:/transaction";
    }

    /**
     * Feature 3: Smart NLP Transaction Input
     * Parses a natural-language sentence and pre-fills the transaction form.
     */
    @PostMapping("/transaction/nlp")
    public String parseNlpTransaction(
            @RequestParam String nlpInput,
            HttpSession session
    ) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        Transaction parsed = nlpParser.parse(nlpInput);
        if (parsed == null) {
            // Could not parse — redirect back with error flag
            return "redirect:/transaction?nlpError=true";
        }

        // Pre-fill the form via query parameters
        String redirect = "redirect:/transaction?nlpAmount=" + parsed.getAmount()
            + "&nlpCategory=" + java.net.URLEncoder.encode(parsed.getCategory(), java.nio.charset.StandardCharsets.UTF_8)
            + "&nlpDate=" + parsed.getDate()
            + "&nlpType=" + parsed.getType()
            + "&nlpDescription=" + java.net.URLEncoder.encode(parsed.getDescription(), java.nio.charset.StandardCharsets.UTF_8);
        return redirect;
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