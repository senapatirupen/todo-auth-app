package com.example.todo.auth.repository;

import com.example.todo.auth.entity.EMI;
import com.example.todo.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EMIRepository extends JpaRepository<EMI, Long> {
    List<EMI> findByUser(User user);
    List<EMI> findByUserOrderByCreatedAtDesc(User user);
    Optional<EMI> findByIdAndUser(Long id, User user);
    void deleteByIdAndUser(Long id, User user);

    @Query("SELECT SUM(e.emiAmount) FROM EMI e WHERE e.user = :user AND e.remainingTenure > 0")
    Optional<Double> findTotalMonthlyEMIByUser(@Param("user") User user);
}
