package com.example.todo.auth.repository;

import com.example.todo.auth.entity.InvestmentOption;
import com.example.todo.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvestmentOptionRepository extends JpaRepository<InvestmentOption, Long> {
    List<InvestmentOption> findByUser(User user);
    List<InvestmentOption> findByUserOrderByCreatedAtDesc(User user);
    Optional<InvestmentOption> findByIdAndUser(Long id, User user);
    void deleteByIdAndUser(Long id, User user);

    List<InvestmentOption> findByUserAndCategory(User user, InvestmentOption.InvestmentCategory category);
    List<InvestmentOption> findByUserAndRiskLevel(User user, InvestmentOption.RiskLevel riskLevel);
    List<InvestmentOption> findByUserAndLiquidity(User user, InvestmentOption.Liquidity liquidity);

    @Query("SELECT io FROM InvestmentOption io WHERE io.user = :user AND io.minCAGR >= :minCAGR AND io.maxCAGR <= :maxCAGR")
    List<InvestmentOption> findByUserAndCAGRRange(@Param("user") User user,
                                                  @Param("minCAGR") Double minCAGR,
                                                  @Param("maxCAGR") Double maxCAGR);

    @Query("SELECT COUNT(io) FROM InvestmentOption io WHERE io.user = :user AND io.category = :category")
    Long countByUserAndCategory(@Param("user") User user,
                                @Param("category") InvestmentOption.InvestmentCategory category);

    @Query("SELECT AVG((io.minCAGR + io.maxCAGR) / 2) FROM InvestmentOption io WHERE io.user = :user")
    Optional<Double> findAverageCAGRByUser(@Param("user") User user);

    @Query("SELECT io.category, COUNT(io) FROM InvestmentOption io WHERE io.user = :user GROUP BY io.category")
    List<Object[]> countByCategoryForUser(@Param("user") User user);
}
