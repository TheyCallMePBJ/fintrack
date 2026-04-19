package com.financetracker.fintrack.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double targetAmount;
    private double currentSaved;
    private LocalDate targetDate;
    private LocalDate createdDate;

    // ===== GETTERS & SETTERS =====
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

    public double getCurrentSaved() { return currentSaved; }
    public void setCurrentSaved(double currentSaved) { this.currentSaved = currentSaved; }

    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    // ===== DERIVED HELPERS (used in templates) =====
    public double getProgressPercent() {
        if (targetAmount <= 0) return 0;
        double pct = (currentSaved / targetAmount) * 100.0;
        return Math.min(pct, 100.0);
    }

    public long getDaysLeft() {
        if (targetDate == null) return 0;
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), targetDate);
        return Math.max(days, 0);
    }

    public double getRemaining() {
        return Math.max(targetAmount - currentSaved, 0);
    }
}
