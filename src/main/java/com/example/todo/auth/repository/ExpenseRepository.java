package com.example.todo.auth.repository;

import com.example.todo.auth.entity.Expense;
import com.example.todo.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUser(User user);
    List<Expense> findByUserOrderByDateDesc(User user);
    List<Expense> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
    List<Expense> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate startDate, LocalDate endDate);
    List<Expense> findByUserAndCategory(User user, String category);
    Optional<Expense> findByIdAndUser(Long id, User user);
    void deleteByIdAndUser(Long id, User user);
}
