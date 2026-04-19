package com.financetracker.fintrack.service;

import com.financetracker.fintrack.model.Goal;
import com.financetracker.fintrack.repository.GoalRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class GoalService {

    private final GoalRepository repo;

    public GoalService(GoalRepository repo) {
        this.repo = repo;
    }

    public Goal save(Goal g) {
        if (g.getCreatedDate() == null) {
            g.setCreatedDate(LocalDate.now());
        }
        return repo.save(g);
    }

    public List<Goal> getAll() {
        return repo.findAll();
    }

    public Goal getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    /**
     * Adds an amount to a goal's current saved balance.
     */
    public void deposit(Long id, double amount) {
        Goal g = getById(id);
        if (g != null) {
            g.setCurrentSaved(g.getCurrentSaved() + amount);
            repo.save(g);
        }
    }

    /**
     * Predicts goal status: "On Track", "At Risk", "Behind", or "Achieved".
     */
    public String getPrediction(Goal g) {
        if (g.getCurrentSaved() >= g.getTargetAmount()) return "Achieved";

        LocalDate now = LocalDate.now();
        long daysLeft = ChronoUnit.DAYS.between(now, g.getTargetDate());
        if (daysLeft <= 0) return "Behind";

        long daysSinceCreated = ChronoUnit.DAYS.between(g.getCreatedDate(), now);
        if (daysSinceCreated <= 0) {
            // Brand new goal — can't compute rate yet; assume on track
            return "On Track";
        }

        double avgDailySavings = g.getCurrentSaved() / daysSinceCreated;
        double projectedTotal = g.getCurrentSaved() + (avgDailySavings * daysLeft);
        double shortfall = g.getTargetAmount() - projectedTotal;
        double shortfallPct = shortfall / g.getTargetAmount();

        if (shortfallPct <= 0) return "On Track";
        if (shortfallPct <= 0.20) return "At Risk";
        return "Behind";
    }
}
