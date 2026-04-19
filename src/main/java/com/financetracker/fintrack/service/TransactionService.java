package com.financetracker.fintrack.service;

import com.financetracker.fintrack.model.Transaction;
import com.financetracker.fintrack.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

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

    // =========================================================
    // FEATURE 1: FINANCIAL BEHAVIOR ANALYZER
    // =========================================================

    /**
     * Returns a list of human-readable behavioral insights derived from expense data.
     */
    public List<String> getBehaviorInsights() {
        List<String> insights = new ArrayList<>();

        // Last 90 days of expenses
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(90);
        List<Transaction> expenses = repo.findByTypeAndDateBetweenOrderByDateAsc("expense", start, end);

        if (expenses.isEmpty()) {
            insights.add("📊 Not enough data yet — add more expenses to unlock your behavior insights.");
            return insights;
        }

        // --- Insight 1: Weekend vs Weekday Spending ---
        double weekendTotal = expenses.stream()
            .filter(t -> {
                DayOfWeek d = t.getDate().getDayOfWeek();
                return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
            })
            .mapToDouble(Transaction::getAmount).sum();

        double weekdayTotal = expenses.stream()
            .filter(t -> {
                DayOfWeek d = t.getDate().getDayOfWeek();
                return d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY;
            })
            .mapToDouble(Transaction::getAmount).sum();

        long weekendDays = expenses.stream()
            .filter(t -> {
                DayOfWeek d = t.getDate().getDayOfWeek();
                return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
            })
            .map(Transaction::getDate).distinct().count();
        long weekdayDays = expenses.stream()
            .filter(t -> {
                DayOfWeek d = t.getDate().getDayOfWeek();
                return d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY;
            })
            .map(Transaction::getDate).distinct().count();

        if (weekendDays > 0 && weekdayDays > 0) {
            double avgWeekend = weekendTotal / weekendDays;
            double avgWeekday = weekdayTotal / weekdayDays;
            if (avgWeekend > avgWeekday * 1.2) {
                long pct = Math.round(((avgWeekend - avgWeekday) / avgWeekday) * 100);
                insights.add("🛍️ Weekend Spender — you spend " + pct + "% more on weekends than weekdays (avg ₹" +
                    String.format("%.0f", avgWeekend) + " vs ₹" + String.format("%.0f", avgWeekday) + "/day).");
            } else if (avgWeekday > avgWeekend * 1.2) {
                insights.add("💼 Weekday Spender — your weekday spending is higher than weekends. Consider reviewing work-related costs.");
            } else {
                insights.add("⚖️ Balanced Spender — your weekend and weekday spending is fairly consistent.");
            }
        }

        // --- Insight 2: Top Category ---
        Map<String, Double> catTotals = expenses.stream()
            .collect(Collectors.groupingBy(
                t -> (t.getCategory() != null && !t.getCategory().isEmpty()) ? t.getCategory() : "Other",
                Collectors.summingDouble(Transaction::getAmount)
            ));
        if (!catTotals.isEmpty()) {
            String topCat = Collections.max(catTotals.entrySet(), Map.Entry.comparingByValue()).getKey();
            double topAmt = catTotals.get(topCat);
            double totalAmt = catTotals.values().stream().mapToDouble(Double::doubleValue).sum();
            long catPct = Math.round((topAmt / totalAmt) * 100);
            insights.add("🏆 Top Category — " + catPct + "% of your spending (₹" +
                String.format("%.0f", topAmt) + ") goes to \"" + topCat + "\".");
        }

        // --- Insight 3: Frequency Trend ---
        long txCount = expenses.size();
        if (txCount >= 10) {
            // Compare first 45 days vs last 45 days frequency
            LocalDate midPoint = start.plusDays(45);
            long firstHalf = expenses.stream().filter(t -> t.getDate().isBefore(midPoint)).count();
            long secondHalf = expenses.stream().filter(t -> !t.getDate().isBefore(midPoint)).count();
            if (secondHalf > firstHalf * 1.3) {
                insights.add("📈 Increasing Frequency — your spending transactions have increased by " +
                    Math.round(((double)(secondHalf - firstHalf) / firstHalf) * 100) + "% compared to 45 days ago.");
            } else if (firstHalf > secondHalf * 1.3) {
                insights.add("📉 Decreasing Frequency — great job! You're making fewer purchases than you were 45 days ago.");
            }
        }

        // --- Insight 4: Largest Single Expense ---
        expenses.stream()
            .max(Comparator.comparingDouble(Transaction::getAmount))
            .ifPresent(largest -> {
                String cat = (largest.getCategory() != null && !largest.getCategory().isEmpty()) ? largest.getCategory() : "Other";
                insights.add("💸 Biggest Expense — ₹" + String.format("%.0f", largest.getAmount()) +
                    " on " + cat + " (" + largest.getDate() + ").");
            });

        // --- Insight 5: Recurring Category Check ---
        Map<String, Long> catCount = expenses.stream()
            .collect(Collectors.groupingBy(
                t -> (t.getCategory() != null && !t.getCategory().isEmpty()) ? t.getCategory() : "Other",
                Collectors.counting()
            ));
        catCount.entrySet().stream()
            .filter(e -> e.getValue() >= 5)
            .max(Map.Entry.comparingByValue())
            .ifPresent(e -> insights.add("🔄 Recurring Habit — you've logged \"" + e.getKey() +
                "\" expenses " + e.getValue() + " times in the last 90 days."));

        return insights;
    }

    /**
     * Returns weekend vs weekday expense totals for chart display.
     * Returns [weekendTotal, weekdayTotal]
     */
    public double[] getWeekendVsWeekday() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(90);
        List<Transaction> expenses = repo.findByTypeAndDateBetweenOrderByDateAsc("expense", start, end);

        double weekendTotal = expenses.stream()
            .filter(t -> t.getDate().getDayOfWeek() == DayOfWeek.SATURDAY
                      || t.getDate().getDayOfWeek() == DayOfWeek.SUNDAY)
            .mapToDouble(Transaction::getAmount).sum();

        double weekdayTotal = expenses.stream()
            .filter(t -> t.getDate().getDayOfWeek() != DayOfWeek.SATURDAY
                      && t.getDate().getDayOfWeek() != DayOfWeek.SUNDAY)
            .mapToDouble(Transaction::getAmount).sum();

        return new double[]{weekendTotal, weekdayTotal};
    }

    /**
     * Returns per-category spending totals for the last 90 days.
     */
    public Map<String, Double> getCategoryTotals() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(90);
        List<Transaction> expenses = repo.findByTypeAndDateBetweenOrderByDateAsc("expense", start, end);

        return expenses.stream().collect(Collectors.groupingBy(
            t -> (t.getCategory() != null && !t.getCategory().isEmpty()) ? t.getCategory() : "Other",
            Collectors.summingDouble(Transaction::getAmount)
        ));
    }

    // =========================================================
    // FEATURE 2: EXPENSE PREDICTION ENGINE
    // =========================================================

    /**
     * Returns total expense for N months ago (0 = current month).
     */
    public double getMonthExpense(int monthsAgo) {
        YearMonth ym = YearMonth.now().minusMonths(monthsAgo);
        return repo.findByTypeAndDateBetween("expense", ym.atDay(1), ym.atEndOfMonth())
                   .stream().mapToDouble(Transaction::getAmount).sum();
    }

    /**
     * Predicts next month's total expense using a weighted 3-month average.
     * Weights: most recent = 50%, 2 months ago = 30%, 3 months ago = 20%.
     */
    public double getPredictedNextMonthExpense() {
        double m1 = getMonthExpense(0); // current month
        double m2 = getMonthExpense(1); // 1 month ago
        double m3 = getMonthExpense(2); // 2 months ago

        boolean hasM2 = m2 > 0;
        boolean hasM3 = m3 > 0;

        if (!hasM2 && !hasM3) return m1; // only current month data
        if (!hasM3) return m1 * 0.6 + m2 * 0.4;
        return m1 * 0.5 + m2 * 0.3 + m3 * 0.2;
    }

    /**
     * Returns last N months expense totals (newest first) for the sparkline.
     */
    public List<Double> getMonthlyExpenseTotals(int months) {
        List<Double> totals = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            totals.add(getMonthExpense(i));
        }
        return totals;
    }

    /**
     * Calculates percent change: (current - previous) / previous * 100.
     * Returns 0 if no previous data.
     */
    public double getExpenseTrendPercent() {
        double current = getMonthExpense(0);
        double previous = getMonthExpense(1);
        if (previous == 0) return 0;
        return ((current - previous) / previous) * 100.0;
    }
}