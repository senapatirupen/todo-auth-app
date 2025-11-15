package com.example.todo.auth.repository;

import com.example.todo.auth.entity.Goal;
import com.example.todo.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUser(User user);
    List<Goal> findByUserOrderByCreatedAtDesc(User user);
    List<Goal> findByUserOrderByCategoryAscCreatedAtDesc(User user);
    Optional<Goal> findByIdAndUser(Long id, User user);
    void deleteByIdAndUser(Long id, User user);

    List<Goal> findByUserAndCategory(User user, Goal.GoalCategory category);

    @Query("SELECT g FROM Goal g WHERE g.user = :user AND g.duration <= :maxDuration")
    List<Goal> findByUserAndDurationLessThanEqual(@Param("user") User user, @Param("maxDuration") Integer maxDuration);

    @Query("SELECT g FROM Goal g WHERE g.user = :user AND g.targetAmount <= :maxAmount")
    List<Goal> findByUserAndTargetAmountLessThanEqual(@Param("user") User user, @Param("maxAmount") Double maxAmount);

    @Query("SELECT COUNT(g) FROM Goal g WHERE g.user = :user AND g.category = :category")
    Long countByUserAndCategory(@Param("user") User user, @Param("category") Goal.GoalCategory category);

    @Query("SELECT SUM(g.targetAmount) FROM Goal g WHERE g.user = :user")
    Optional<Double> findTotalTargetAmountByUser(@Param("user") User user);

    @Query("SELECT g.category, COUNT(g) FROM Goal g WHERE g.user = :user GROUP BY g.category")
    List<Object[]> countByCategoryForUser(@Param("user") User user);

    @Query("SELECT g.category, SUM(g.targetAmount) FROM Goal g WHERE g.user = :user GROUP BY g.category")
    List<Object[]> sumTargetAmountByCategoryForUser(@Param("user") User user);
}
