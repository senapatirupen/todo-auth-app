package com.example.todo.auth.controller;

import com.example.todo.auth.entity.Goal;
import com.example.todo.auth.service.GoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
public class GoalController {

    @Autowired
    private GoalService goalService;

    @PostMapping
    public ResponseEntity<?> createGoal(@RequestBody Goal goal, Principal principal) {
        try {
            Goal savedGoal = goalService.createGoal(goal, principal.getName());
            return ResponseEntity.ok(savedGoal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating goal: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Goal>> getUserGoals(Principal principal) {
        try {
            List<Goal> goals = goalService.getUserGoals(principal.getName());
            return ResponseEntity.ok(goals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGoalById(@PathVariable Long id, Principal principal) {
        try {
            Goal goal = goalService.getGoalByIdAndUser(id, principal.getName());
            return ResponseEntity.ok(goal);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGoal(@PathVariable Long id, @RequestBody Goal goal, Principal principal) {
        try {
            Goal updatedGoal = goalService.updateGoal(id, goal, principal.getName());
            return ResponseEntity.ok(updatedGoal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating goal: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@PathVariable Long id, Principal principal) {
        try {
            goalService.deleteGoal(id, principal.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting goal: " + e.getMessage());
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> getGoalsByCategory(@PathVariable String category, Principal principal) {
        try {
            Goal.GoalCategory goalCategory = Goal.GoalCategory.valueOf(category.toUpperCase());
            List<Goal> goals = goalService.getGoalsByCategory(principal.getName(), goalCategory);
            return ResponseEntity.ok(goals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid category or error retrieving goals: " + e.getMessage());
        }
    }

    @GetMapping("/short-term")
    public ResponseEntity<List<Goal>> getShortTermGoals(Principal principal) {
        try {
            List<Goal> goals = goalService.getShortTermGoals(principal.getName());
            return ResponseEntity.ok(goals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/medium-term")
    public ResponseEntity<List<Goal>> getMediumTermGoals(Principal principal) {
        try {
            List<Goal> goals = goalService.getMediumTermGoals(principal.getName());
            return ResponseEntity.ok(goals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/long-term")
    public ResponseEntity<List<Goal>> getLongTermGoals(Principal principal) {
        try {
            List<Goal> goals = goalService.getLongTermGoals(principal.getName());
            return ResponseEntity.ok(goals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/retirement")
    public ResponseEntity<List<Goal>> getRetirementGoals(Principal principal) {
        try {
            List<Goal> goals = goalService.getRetirementGoals(principal.getName());
            return ResponseEntity.ok(goals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/summary/overview")
    public ResponseEntity<Map<String, Object>> getGoalsSummary(Principal principal) {
        try {
            Map<String, Object> summary = goalService.getGoalsSummary(principal.getName());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyMap());
        }
    }

    @GetMapping("/{id}/planning")
    public ResponseEntity<?> calculateGoalPlanning(
            @PathVariable Long id,
            @RequestParam Double expectedReturn,
            Principal principal) {
        try {
            Map<String, Object> planning = goalService.calculateGoalPlanning(id, expectedReturn, principal.getName());
            return ResponseEntity.ok(planning);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error calculating goal planning: " + e.getMessage());
        }
    }

    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingGoals(
            @RequestParam(defaultValue = "5") int yearsAhead,
            Principal principal) {
        try {
            List<Map<String, Object>> upcomingGoals = goalService.getUpcomingGoals(principal.getName(), yearsAhead);
            return ResponseEntity.ok(upcomingGoals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving upcoming goals: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/track-progress")
    public ResponseEntity<?> trackGoalProgress(
            @PathVariable Long id,
            @RequestParam Double currentSavings,
            Principal principal) {
        try {
            Map<String, Object> progress = goalService.trackGoalProgress(id, currentSavings, principal.getName());
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error tracking goal progress: " + e.getMessage());
        }
    }
}
