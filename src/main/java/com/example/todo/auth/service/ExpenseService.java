package com.example.todo.auth.service;

import com.example.todo.auth.entity.Expense;
import com.example.todo.auth.entity.User;
import com.example.todo.auth.repository.ExpenseRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserService userService;

    public Expense createExpense(Expense expense, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Set the user for the expense
        expense.setUser(user);

        // Set current date if not provided
        if (expense.getDate() == null) {
            expense.setDate(LocalDate.now());
        }

        return expenseRepository.save(expense);
    }

    @Transactional(readOnly = true)
    public List<Expense> getUserExpenses(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return expenseRepository.findByUserOrderByDateDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Expense> getMonthlyExpenses(String username, int year, int month) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        return expenseRepository.findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate);
    }

    public Expense updateExpense(Long id, Expense expenseDetails, String username) {
        // First verify the expense belongs to the current user
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        // Security check: ensure the expense belongs to the current user
        if (!expense.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You are not authorized to update this expense");
        }

        // Update fields
        expense.setCategory(expenseDetails.getCategory());
        expense.setAmount(expenseDetails.getAmount());
        expense.setDate(expenseDetails.getDate());
        expense.setDescription(expenseDetails.getDescription());
        expense.setInflationRate(expenseDetails.getInflationRate());

        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long id, String username) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        // Security check: ensure the expense belongs to the current user
        if (!expense.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You are not authorized to delete this expense");
        }

        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public Map<String, Double> getCategoryWiseExpenses(String username, int year, int month) {
        List<Expense> monthlyExpenses = getMonthlyExpenses(username, year, month);

        return monthlyExpenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }

    @Transactional(readOnly = true)
    public Expense getExpenseByIdAndUser(Long id, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return expenseRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Double getTotalMonthlyExpenses(String username, int year, int month) {
        List<Expense> monthlyExpenses = getMonthlyExpenses(username, year, month);
        return monthlyExpenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();
    }
}