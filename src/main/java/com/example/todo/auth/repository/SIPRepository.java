package com.example.todo.auth.repository;

import com.example.todo.auth.entity.SIP;
import com.example.todo.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SIPRepository extends JpaRepository<SIP, Long> {
    List<SIP> findByUser(User user);
    List<SIP> findByUserOrderByCreatedAtDesc(User user);
    Optional<SIP> findByIdAndUser(Long id, User user);
    void deleteByIdAndUser(Long id, User user);

    @Query("SELECT SUM(s.monthlyInvestment) FROM SIP s WHERE s.user = :user")
    Optional<Double> findTotalMonthlyInvestmentByUser(@Param("user") User user);

    @Query("SELECT SUM(s.futureValue) FROM SIP s WHERE s.user = :user")
    Optional<Double> findTotalFutureValueByUser(@Param("user") User user);

    @Query("SELECT SUM(s.totalInvestment) FROM SIP s WHERE s.user = :user")
    Optional<Double> findTotalInvestmentByUser(@Param("user") User user);
}
