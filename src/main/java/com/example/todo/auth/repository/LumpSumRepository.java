package com.example.todo.auth.repository;

import com.example.todo.auth.entity.LumpSum;
import com.example.todo.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LumpSumRepository extends JpaRepository<LumpSum, Long> {
    List<LumpSum> findByUser(User user);
    List<LumpSum> findByUserOrderByCreatedAtDesc(User user);
    Optional<LumpSum> findByIdAndUser(Long id, User user);
    void deleteByIdAndUser(Long id, User user);

    @Query("SELECT SUM(ls.principalAmount) FROM LumpSum ls WHERE ls.user = :user")
    Optional<Double> findTotalPrincipalByUser(@Param("user") User user);

    @Query("SELECT SUM(ls.futureValue) FROM LumpSum ls WHERE ls.user = :user")
    Optional<Double> findTotalFutureValueByUser(@Param("user") User user);

    @Query("SELECT SUM(ls.totalInterest) FROM LumpSum ls WHERE ls.user = :user")
    Optional<Double> findTotalInterestByUser(@Param("user") User user);

    @Query("SELECT AVG(ls.expectedReturn) FROM LumpSum ls WHERE ls.user = :user")
    Optional<Double> findAverageReturnByUser(@Param("user") User user);
}
