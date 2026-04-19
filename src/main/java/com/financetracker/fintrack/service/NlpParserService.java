package com.financetracker.fintrack.service;

import com.financetracker.fintrack.model.Transaction;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Smart NLP Parser — converts plain-English sentences into Transaction objects.
 * Example: "Spent 500 on food yesterday" → {amount=500, category=Dining, date=yesterday}
 */
@Service
public class NlpParserService {

    // Category keyword map
    private static final Map<String, String> CATEGORY_KEYWORDS = Map.ofEntries(
        Map.entry("food", "Dining"),
        Map.entry("lunch", "Dining"),
        Map.entry("dinner", "Dining"),
        Map.entry("breakfast", "Dining"),
        Map.entry("restaurant", "Dining"),
        Map.entry("cafe", "Dining"),
        Map.entry("coffee", "Dining"),
        Map.entry("eat", "Dining"),
        Map.entry("groceries", "Groceries"),
        Map.entry("grocery", "Groceries"),
        Map.entry("supermarket", "Groceries"),
        Map.entry("vegetables", "Groceries"),
        Map.entry("fruits", "Groceries"),
        Map.entry("milk", "Groceries"),
        Map.entry("petrol", "Transport"),
        Map.entry("fuel", "Transport"),
        Map.entry("uber", "Transport"),
        Map.entry("ola", "Transport"),
        Map.entry("taxi", "Transport"),
        Map.entry("bus", "Transport"),
        Map.entry("metro", "Transport"),
        Map.entry("auto", "Transport"),
        Map.entry("transport", "Transport"),
        Map.entry("travel", "Transport"),
        Map.entry("electricity", "Utilities"),
        Map.entry("electric", "Utilities"),
        Map.entry("water", "Utilities"),
        Map.entry("gas", "Utilities"),
        Map.entry("internet", "Utilities"),
        Map.entry("wifi", "Utilities"),
        Map.entry("bill", "Utilities"),
        Map.entry("recharge", "Utilities"),
        Map.entry("movie", "Entertainment"),
        Map.entry("netflix", "Entertainment"),
        Map.entry("cinema", "Entertainment"),
        Map.entry("game", "Entertainment"),
        Map.entry("gaming", "Entertainment"),
        Map.entry("concert", "Entertainment"),
        Map.entry("sports", "Entertainment"),
        Map.entry("shopping", "Shopping"),
        Map.entry("clothes", "Shopping"),
        Map.entry("shoes", "Shopping"),
        Map.entry("amazon", "Shopping"),
        Map.entry("flipkart", "Shopping"),
        Map.entry("hospital", "Health"),
        Map.entry("doctor", "Health"),
        Map.entry("medicine", "Health"),
        Map.entry("pharmacy", "Health"),
        Map.entry("medical", "Health"),
        Map.entry("gym", "Health"),
        Map.entry("rent", "Housing"),
        Map.entry("house", "Housing"),
        Map.entry("flat", "Housing"),
        Map.entry("school", "Education"),
        Map.entry("college", "Education"),
        Map.entry("course", "Education"),
        Map.entry("book", "Education"),
        Map.entry("books", "Education"),
        Map.entry("fees", "Education")
    );

    // Date keyword map (relative to today)
    private static final Map<String, Integer> DATE_OFFSETS = Map.of(
        "today", 0,
        "yesterday", -1,
        "day before yesterday", -2,
        "last week", -7,
        "this week", 0,
        "last month", -30
    );

    /**
     * Parses a natural-language expense input into a Transaction object.
     * Returns null if amount cannot be extracted.
     */
    public Transaction parse(String input) {
        if (input == null || input.trim().isEmpty()) return null;

        String text = input.toLowerCase().trim();

        // 1. Extract amount
        Double amount = extractAmount(text);
        if (amount == null) return null;

        // 2. Extract date
        LocalDate date = extractDate(text);

        // 3. Extract category
        String category = extractCategory(text);

        // 4. Build description from input
        String description = input.trim();

        Transaction t = new Transaction();
        t.setAmount(amount);
        t.setDate(date);
        t.setCategory(category);
        t.setDescription(description);
        
        // Detect type
        String lowerInput = input.toLowerCase();
        if (lowerInput.contains("earn") || lowerInput.contains("salary") || lowerInput.contains("received") || lowerInput.contains("got") || lowerInput.contains("income") || lowerInput.contains("profit") || lowerInput.contains("gift")) {
            t.setType("income");
        } else {
            t.setType("expense");
        }

        return t;
    }

    private Double extractAmount(String text) {
        // Matches patterns like: 500, 1500.50, ₹500, rs 500, inr 500
        Pattern p = Pattern.compile("(?:₹|rs\\.?\\s*|inr\\s*)?(\\d{1,8}(?:\\.\\d{1,2})?)");
        Matcher m = p.matcher(text);
        Double largest = null;
        while (m.find()) {
            try {
                double val = Double.parseDouble(m.group(1));
                // Filter out years (1900-2100) to avoid picking up year references as amounts
                if (val >= 1900 && val <= 2100 && m.group(1).length() == 4) continue;
                if (largest == null || val > largest) largest = val;
            } catch (NumberFormatException ignored) {}
        }
        return largest;
    }

    private LocalDate extractDate(String text) {
        // Check multi-word patterns first
        for (Map.Entry<String, Integer> entry : DATE_OFFSETS.entrySet()) {
            if (text.contains(entry.getKey())) {
                return LocalDate.now().plusDays(entry.getValue());
            }
        }
        // Check day-of-week patterns (e.g., "last monday")
        String[] days = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        int todayDow = LocalDate.now().getDayOfWeek().getValue(); // 1=Mon..7=Sun
        for (int i = 0; i < days.length; i++) {
            if (text.contains(days[i])) {
                int targetDow = i + 1; // 1=Mon..7=Sun
                int diff = todayDow - targetDow;
                if (diff <= 0) diff += 7;
                return LocalDate.now().minusDays(diff);
            }
        }
        // Default to today
        return LocalDate.now();
    }

    private String extractCategory(String text) {
        // Check longest match first by iterating sorted keys
        for (Map.Entry<String, String> entry : CATEGORY_KEYWORDS.entrySet()) {
            if (text.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "Other";
    }
}
