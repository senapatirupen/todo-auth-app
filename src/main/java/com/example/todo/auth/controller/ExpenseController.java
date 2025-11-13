package com.example.todo.auth.controller;

import com.example.todo.auth.entity.Expense;
import com.example.todo.auth.service.ExpenseService;
import com.example.todo.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600, allowCredentials = "true")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<?> createExpense(@RequestBody Expense expense, Principal principal) {
        try {
            Expense savedExpense = expenseService.createExpense(expense, principal.getName());
            return ResponseEntity.ok(savedExpense);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating expense: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getUserExpenses(Principal principal) {
        try {
            List<Expense> expenses = expenseService.getUserExpenses(principal.getName());
            return ResponseEntity.ok(expenses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<Expense>> getMonthlyExpenses(
            @RequestParam int year,
            @RequestParam int month,
            Principal principal) {
        try {
            List<Expense> expenses = expenseService.getMonthlyExpenses(principal.getName(), year, month);
            return ResponseEntity.ok(expenses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<String, Double>> getCategoryWiseExpenses(
            @RequestParam int year,
            @RequestParam int month,
            Principal principal) {
        try {
            Map<String, Double> categoryExpenses = expenseService.getCategoryWiseExpenses(principal.getName(), year, month);
            return ResponseEntity.ok(categoryExpenses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyMap());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getExpenseById(@PathVariable Long id, Principal principal) {
        try {
            Expense expense = expenseService.getExpenseByIdAndUser(id, principal.getName());
            return ResponseEntity.ok(expense);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/total/monthly")
    public ResponseEntity<Double> getTotalMonthlyExpenses(
            @RequestParam int year,
            @RequestParam int month,
            Principal principal) {
        try {
            Double total = expenseService.getTotalMonthlyExpenses(principal.getName(), year, month);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(0.0);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpense(@PathVariable Long id, @RequestBody Expense expense, Principal principal) {
        try {
            Expense updatedExpense = expenseService.updateExpense(id, expense, principal.getName());
            return ResponseEntity.ok(updatedExpense);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating expense: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id, Principal principal) {
        try {
            expenseService.deleteExpense(id, principal.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting expense: " + e.getMessage());
        }
    }
}