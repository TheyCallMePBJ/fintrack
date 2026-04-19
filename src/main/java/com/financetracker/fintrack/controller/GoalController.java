package com.financetracker.fintrack.controller;

import com.financetracker.fintrack.model.Goal;
import com.financetracker.fintrack.service.GoalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;

@Controller
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @GetMapping("/goals")
    public String goalsPage(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        List<Goal> goals = goalService.getAll();

        // Attach predictions to each goal via model attribute map
        java.util.Map<Long, String> predictions = new java.util.LinkedHashMap<>();
        for (Goal g : goals) {
            predictions.put(g.getId(), goalService.getPrediction(g));
        }

        model.addAttribute("goals", goals);
        model.addAttribute("predictions", predictions);

        return "goals";
    }

    @PostMapping("/goals")
    public String addGoal(
            @RequestParam String name,
            @RequestParam double targetAmount,
            @RequestParam String targetDate,
            HttpSession session
    ) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        Goal g = new Goal();
        g.setName(name);
        g.setTargetAmount(targetAmount);
        g.setCurrentSaved(0);
        g.setTargetDate(LocalDate.parse(targetDate));
        g.setCreatedDate(LocalDate.now());

        goalService.save(g);
        return "redirect:/goals";
    }

    @PostMapping("/goals/delete/{id}")
    public String deleteGoal(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        goalService.delete(id);
        return "redirect:/goals";
    }

    @PostMapping("/goals/deposit/{id}")
    public String depositToGoal(
            @PathVariable Long id,
            @RequestParam double amount,
            HttpSession session
    ) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        goalService.deposit(id, amount);
        return "redirect:/goals";
    }
}
