package com.example.todo.auth.service;

import com.example.todo.auth.entity.InvestmentOption;
import com.example.todo.auth.entity.User;
import com.example.todo.auth.repository.InvestmentOptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class InvestmentOptionService {

    @Autowired
    private InvestmentOptionRepository investmentOptionRepository;

    @Autowired
    private UserService userService;

    public InvestmentOption createInvestmentOption(InvestmentOption investmentOption, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        investmentOption.setUser(user);
        return investmentOptionRepository.save(investmentOption);
    }

    @Transactional(readOnly = true)
    public List<InvestmentOption> getUserInvestmentOptions(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return investmentOptionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public InvestmentOption getInvestmentOptionByIdAndUser(Long id, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return investmentOptionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Investment option not found with id: " + id));
    }

    public InvestmentOption updateInvestmentOption(Long id, InvestmentOption investmentOptionDetails, String username) {
        InvestmentOption investmentOption = getInvestmentOptionByIdAndUser(id, username);

        // Update all fields
        investmentOption.setName(investmentOptionDetails.getName());
        investmentOption.setCategory(investmentOptionDetails.getCategory());
        investmentOption.setMinCAGR(investmentOptionDetails.getMinCAGR());
        investmentOption.setMaxCAGR(investmentOptionDetails.getMaxCAGR());
        investmentOption.setRiskLevel(investmentOptionDetails.getRiskLevel());
        investmentOption.setLiquidity(investmentOptionDetails.getLiquidity());
        investmentOption.setTaxEfficiency(investmentOptionDetails.getTaxEfficiency());
        investmentOption.setNotes(investmentOptionDetails.getNotes());

        return investmentOptionRepository.save(investmentOption);
    }

    public void deleteInvestmentOption(Long id, String username) {
        InvestmentOption investmentOption = getInvestmentOptionByIdAndUser(id, username);
        investmentOptionRepository.delete(investmentOption);
    }

    @Transactional(readOnly = true)
    public List<InvestmentOption> getInvestmentOptionsByCategory(String username, InvestmentOption.InvestmentCategory category) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return investmentOptionRepository.findByUserAndCategory(user, category);
    }

    @Transactional(readOnly = true)
    public List<InvestmentOption> getInvestmentOptionsByRiskLevel(String username, InvestmentOption.RiskLevel riskLevel) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return investmentOptionRepository.findByUserAndRiskLevel(user, riskLevel);
    }

    @Transactional(readOnly = true)
    public List<InvestmentOption> getInvestmentOptionsByLiquidity(String username, InvestmentOption.Liquidity liquidity) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return investmentOptionRepository.findByUserAndLiquidity(user, liquidity);
    }

    @Transactional(readOnly = true)
    public List<InvestmentOption> getInvestmentOptionsByCAGRRange(String username, Double minCAGR, Double maxCAGR) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return investmentOptionRepository.findByUserAndCAGRRange(user, minCAGR, maxCAGR);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getInvestmentOptionsSummary(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<InvestmentOption> investmentOptions = investmentOptionRepository.findByUser(user);

        double averageCAGR = investmentOptionRepository.findAverageCAGRByUser(user).orElse(0.0);

        // Count by category
        Map<InvestmentOption.InvestmentCategory, Long> categoryCounts = new HashMap<>();
        List<Object[]> categoryCountsResult = investmentOptionRepository.countByCategoryForUser(user);
        for (Object[] result : categoryCountsResult) {
            InvestmentOption.InvestmentCategory category = (InvestmentOption.InvestmentCategory) result[0];
            Long count = (Long) result[1];
            categoryCounts.put(category, count);
        }

        // Count by risk level
        Map<InvestmentOption.RiskLevel, Long> riskLevelCounts = investmentOptions.stream()
                .collect(Collectors.groupingBy(InvestmentOption::getRiskLevel, Collectors.counting()));

        // Count by liquidity
        Map<InvestmentOption.Liquidity, Long> liquidityCounts = investmentOptions.stream()
                .collect(Collectors.groupingBy(InvestmentOption::getLiquidity, Collectors.counting()));

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalInvestmentOptions", investmentOptions.size());
        summary.put("averageCAGR", Math.round(averageCAGR * 100.0) / 100.0);
        summary.put("categoryDistribution", categoryCounts);
        summary.put("riskDistribution", riskLevelCounts);
        summary.put("liquidityDistribution", liquidityCounts);

        return summary;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecommendedOptions(String username,
                                                           InvestmentOption.RiskLevel preferredRiskLevel,
                                                           InvestmentOption.Liquidity preferredLiquidity,
                                                           Double minExpectedCAGR) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<InvestmentOption> allOptions = investmentOptionRepository.findByUser(user);

        return allOptions.stream()
                .filter(option -> option.getRiskLevel().ordinal() <= preferredRiskLevel.ordinal())
                .filter(option -> option.getLiquidity().ordinal() >= preferredLiquidity.ordinal())
                .filter(option -> option.getAverageCAGR() >= minExpectedCAGR)
                .sorted((o1, o2) -> Double.compare(o2.getAverageCAGR(), o1.getAverageCAGR()))
                .map(option -> {
                    Map<String, Object> recommendation = new HashMap<>();
                    recommendation.put("id", option.getId());
                    recommendation.put("name", option.getName());
                    recommendation.put("category", option.getCategory());
                    recommendation.put("averageCAGR", Math.round(option.getAverageCAGR() * 100.0) / 100.0);
                    recommendation.put("riskLevel", option.getRiskLevel());
                    recommendation.put("liquidity", option.getLiquidity());
                    recommendation.put("matchScore", calculateMatchScore(option, preferredRiskLevel, preferredLiquidity, minExpectedCAGR));
                    return recommendation;
                })
                .collect(Collectors.toList());
    }

    private int calculateMatchScore(InvestmentOption option,
                                    InvestmentOption.RiskLevel preferredRiskLevel,
                                    InvestmentOption.Liquidity preferredLiquidity,
                                    Double minExpectedCAGR) {
        int score = 0;

        // Risk score (lower risk is better if within preferred range)
        if (option.getRiskLevel().ordinal() <= preferredRiskLevel.ordinal()) {
            score += (preferredRiskLevel.ordinal() - option.getRiskLevel().ordinal()) * 10;
        }

        // Liquidity score (higher liquidity is better)
        if (option.getLiquidity().ordinal() >= preferredLiquidity.ordinal()) {
            score += (option.getLiquidity().ordinal() - preferredLiquidity.ordinal()) * 5;
        }

        // CAGR score (higher CAGR is better)
        if (option.getAverageCAGR() >= minExpectedCAGR) {
            score += (int) ((option.getAverageCAGR() - minExpectedCAGR) * 20);
        }

        return score;
    }
}
