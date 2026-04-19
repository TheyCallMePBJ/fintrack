package com.financetracker.fintrack.repository;

import com.financetracker.fintrack.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalRepository extends JpaRepository<Goal, Long> {
}
