package com.example.todo.auth.service;

import com.example.todo.auth.entity.IncomeSource;
import com.example.todo.auth.entity.User;
import com.example.todo.auth.repository.IncomeSourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class IncomeSourceService {

    @Autowired
    private IncomeSourceRepository incomeSourceRepository;

    @Autowired
    private UserService userService;

    public IncomeSource createIncomeSource(IncomeSource incomeSource, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        incomeSource.setUser(user);
        incomeSource.calculateIncomeProjection(); // Calculate all derived fields
        return incomeSourceRepository.save(incomeSource);
    }

    @Transactional(readOnly = true)
    public List<IncomeSource> getUserIncomeSources(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return incomeSourceRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public IncomeSource getIncomeSourceByIdAndUser(Long id, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return incomeSourceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Income source not found with id: " + id));
    }

    public IncomeSource updateIncomeSource(Long id, IncomeSource incomeSourceDetails, String username) {
        IncomeSource incomeSource = getIncomeSourceByIdAndUser(id, username);

        // Update basic fields
        incomeSource.setSourceName(incomeSourceDetails.getSourceName());
        incomeSource.setInitialMonthlyIncome(incomeSourceDetails.getInitialMonthlyIncome());
        incomeSource.setAnnualGrowthRate(incomeSourceDetails.getAnnualGrowthRate());
        incomeSource.setYears(incomeSourceDetails.getYears());

        // Recalculate all derived fields
        incomeSource.calculateIncomeProjection();

        return incomeSourceRepository.save(incomeSource);
    }

    public void deleteIncomeSource(Long id, String username) {
        IncomeSource incomeSource = getIncomeSourceByIdAndUser(id, username);
        incomeSourceRepository.delete(incomeSource);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getIncomeSourceSummary(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<IncomeSource> incomeSources = incomeSourceRepository.findByUser(user);

        double totalInitialMonthlyIncome = incomeSourceRepository.findTotalInitialMonthlyIncomeByUser(user).orElse(0.0);
        double totalProjectedMonthlyIncome = incomeSourceRepository.findTotalProjectedMonthlyIncomeByUser(user).orElse(0.0);
        double totalAmountReceived = incomeSourceRepository.findTotalAmountReceivedByUser(user).orElse(0.0);
        double averageGrowthRate = incomeSourceRepository.findAverageGrowthRateByUser(user).orElse(0.0);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncomeSources", incomeSources.size());
        summary.put("totalInitialMonthlyIncome", Math.round(totalInitialMonthlyIncome * 100.0) / 100.0);
        summary.put("totalProjectedMonthlyIncome", Math.round(totalProjectedMonthlyIncome * 100.0) / 100.0);
        summary.put("totalAmountReceived", Math.round(totalAmountReceived * 100.0) / 100.0);
        summary.put("averageGrowthRate", Math.round(averageGrowthRate * 100.0) / 100.0);
        summary.put("totalGrowthPercentage", totalInitialMonthlyIncome > 0 ?
                Math.round(((totalProjectedMonthlyIncome - totalInitialMonthlyIncome) / totalInitialMonthlyIncome) * 100 * 100.0) / 100.0 : 0.0);

        return summary;
    }

    public List<Map<String, Object>> getIncomeProgression(Long id, String username) {
        IncomeSource incomeSource = getIncomeSourceByIdAndUser(id, username);
        return incomeSource.getYearlyIncomeProgression();
    }

    public Map<String, Object> getIncomeForYear(Long id, Integer targetYear, String username) {
        IncomeSource incomeSource = getIncomeSourceByIdAndUser(id, username);
        return incomeSource.calculateIncomeForYear(targetYear);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getFastestGrowingIncomes(String username, int limit) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<IncomeSource> incomeSources = incomeSourceRepository.findByUser(user);

        return incomeSources.stream()
                .sorted((is1, is2) -> Double.compare(is2.getAnnualGrowthRate(), is1.getAnnualGrowthRate()))
                .limit(limit)
                .map(is -> {
                    Map<String, Object> growthData = new HashMap<>();
                    growthData.put("id", is.getId());
                    growthData.put("sourceName", is.getSourceName());
                    growthData.put("initialMonthlyIncome", is.getInitialMonthlyIncome());
                    growthData.put("projectedMonthlyIncome", is.getProjectedMonthlyIncome());
                    growthData.put("annualGrowthRate", is.getAnnualGrowthRate());
                    growthData.put("totalGrowth", Math.round(((is.getProjectedMonthlyIncome() - is.getInitialMonthlyIncome()) / is.getInitialMonthlyIncome()) * 100 * 100.0) / 100.0);
                    growthData.put("years", is.getYears());
                    return growthData;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getYearlyIncomeSummary(String username, int targetYear) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<IncomeSource> incomeSources = incomeSourceRepository.findByUser(user);

        double totalMonthlyIncome = 0.0;
        double totalAnnualIncome = 0.0;
        List<Map<String, Object>> sourceDetails = new ArrayList<>();

        for (IncomeSource incomeSource : incomeSources) {
            if (targetYear <= incomeSource.getYears()) {
                Map<String, Object> yearData = incomeSource.calculateIncomeForYear(targetYear);
                double monthlyIncome = (Double) yearData.get("monthlyIncome");
                double annualIncome = (Double) yearData.get("annualIncome");

                totalMonthlyIncome += monthlyIncome;
                totalAnnualIncome += annualIncome;

                Map<String, Object> sourceDetail = new HashMap<>();
                sourceDetail.put("sourceName", incomeSource.getSourceName());
                sourceDetail.put("monthlyIncome", monthlyIncome);
                sourceDetail.put("annualIncome", annualIncome);
                sourceDetail.put("growthRate", incomeSource.getAnnualGrowthRate());
                sourceDetails.add(sourceDetail);
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("year", targetYear);
        summary.put("totalMonthlyIncome", Math.round(totalMonthlyIncome * 100.0) / 100.0);
        summary.put("totalAnnualIncome", Math.round(totalAnnualIncome * 100.0) / 100.0);
        summary.put("incomeSources", sourceDetails);

        return summary;
    }
}
