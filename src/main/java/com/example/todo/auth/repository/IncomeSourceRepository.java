package com.example.todo.auth.repository;

import com.example.todo.auth.entity.IncomeSource;
import com.example.todo.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeSourceRepository extends JpaRepository<IncomeSource, Long> {
    List<IncomeSource> findByUser(User user);
    List<IncomeSource> findByUserOrderByCreatedAtDesc(User user);
    Optional<IncomeSource> findByIdAndUser(Long id, User user);
    void deleteByIdAndUser(Long id, User user);

    @Query("SELECT SUM(is.initialMonthlyIncome) FROM IncomeSource is WHERE is.user = :user")
    Optional<Double> findTotalInitialMonthlyIncomeByUser(@Param("user") User user);

    @Query("SELECT SUM(is.projectedMonthlyIncome) FROM IncomeSource is WHERE is.user = :user")
    Optional<Double> findTotalProjectedMonthlyIncomeByUser(@Param("user") User user);

    @Query("SELECT SUM(is.totalAmountReceived) FROM IncomeSource is WHERE is.user = :user")
    Optional<Double> findTotalAmountReceivedByUser(@Param("user") User user);

    @Query("SELECT AVG(is.annualGrowthRate) FROM IncomeSource is WHERE is.user = :user")
    Optional<Double> findAverageGrowthRateByUser(@Param("user") User user);
}
