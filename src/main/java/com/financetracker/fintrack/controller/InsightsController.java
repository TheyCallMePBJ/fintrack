package com.financetracker.fintrack.controller;

import com.financetracker.fintrack.service.TransactionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
public class InsightsController {

    private final TransactionService service;

    public InsightsController(TransactionService service) {
        this.service = service;
    }

    @GetMapping("/insights")
    public String insightsPage(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        List<String> insights = service.getBehaviorInsights();
        double[] weekendVsWeekday = service.getWeekendVsWeekday();
        Map<String, Double> categoryTotals = service.getCategoryTotals();

        model.addAttribute("insights", insights);
        model.addAttribute("weekendTotal", weekendVsWeekday[0]);
        model.addAttribute("weekdayTotal", weekendVsWeekday[1]);
        model.addAttribute("categoryTotals", categoryTotals);

        return "insights";
    }
}
