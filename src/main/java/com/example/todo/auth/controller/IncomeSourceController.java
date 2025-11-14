package com.example.todo.auth.controller;

import com.example.todo.auth.entity.IncomeSource;
import com.example.todo.auth.service.IncomeSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/income-sources")
public class IncomeSourceController {

    @Autowired
    private IncomeSourceService incomeSourceService;

    @PostMapping
    public ResponseEntity<?> createIncomeSource(@RequestBody IncomeSource incomeSource, Principal principal) {
        try {
            IncomeSource savedIncomeSource = incomeSourceService.createIncomeSource(incomeSource, principal.getName());
            return ResponseEntity.ok(savedIncomeSource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating income source: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<IncomeSource>> getUserIncomeSources(Principal principal) {
        try {
            List<IncomeSource> incomeSources = incomeSourceService.getUserIncomeSources(principal.getName());
            return ResponseEntity.ok(incomeSources);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getIncomeSourceById(@PathVariable Long id, Principal principal) {
        try {
            IncomeSource incomeSource = incomeSourceService.getIncomeSourceByIdAndUser(id, principal.getName());
            return ResponseEntity.ok(incomeSource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateIncomeSource(@PathVariable Long id, @RequestBody IncomeSource incomeSource, Principal principal) {
        try {
            IncomeSource updatedIncomeSource = incomeSourceService.updateIncomeSource(id, incomeSource, principal.getName());
            return ResponseEntity.ok(updatedIncomeSource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating income source: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIncomeSource(@PathVariable Long id, Principal principal) {
        try {
            incomeSourceService.deleteIncomeSource(id, principal.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting income source: " + e.getMessage());
        }
    }

    @GetMapping("/summary/overview")
    public ResponseEntity<Map<String, Object>> getIncomeSourceSummary(Principal principal) {
        try {
            Map<String, Object> summary = incomeSourceService.getIncomeSourceSummary(principal.getName());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyMap());
        }
    }

    @GetMapping("/{id}/progression")
    public ResponseEntity<?> getIncomeProgression(@PathVariable Long id, Principal principal) {
        try {
            List<Map<String, Object>> progression = incomeSourceService.getIncomeProgression(id, principal.getName());
            return ResponseEntity.ok(progression);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating income progression: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/yearly-income")
    public ResponseEntity<?> getIncomeForYear(
            @PathVariable Long id,
            @RequestParam Integer targetYear,
            Principal principal) {
        try {
            Map<String, Object> incomeData = incomeSourceService.getIncomeForYear(id, targetYear, principal.getName());
            return ResponseEntity.ok(incomeData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error calculating yearly income: " + e.getMessage());
        }
    }

    @GetMapping("/fastest-growing")
    public ResponseEntity<?> getFastestGrowingIncomes(
            @RequestParam(defaultValue = "5") int limit,
            Principal principal) {
        try {
            List<Map<String, Object>> fastestGrowing = incomeSourceService.getFastestGrowingIncomes(principal.getName(), limit);
            return ResponseEntity.ok(fastestGrowing);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error getting fastest growing incomes: " + e.getMessage());
        }
    }

    @GetMapping("/yearly-summary")
    public ResponseEntity<?> getYearlyIncomeSummary(
            @RequestParam Integer targetYear,
            Principal principal) {
        try {
            Map<String, Object> yearlySummary = incomeSourceService.getYearlyIncomeSummary(principal.getName(), targetYear);
            return ResponseEntity.ok(yearlySummary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating yearly summary: " + e.getMessage());
        }
    }
}
