package com.example.todo.auth.service;

import com.example.todo.auth.entity.Goal;
import com.example.todo.auth.entity.User;
import com.example.todo.auth.repository.GoalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class GoalService {

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private UserService userService;

    public Goal createGoal(Goal goal, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        goal.setUser(user);
        goal.calculateInflationAdjustedAmount(); // Calculate derived fields
        return goalRepository.save(goal);
    }

    @Transactional(readOnly = true)
    public List<Goal> getUserGoals(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return goalRepository.findByUserOrderByCategoryAscCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public Goal getGoalByIdAndUser(Long id, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return goalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Goal not found with id: " + id));
    }

    public Goal updateGoal(Long id, Goal goalDetails, String username) {
        Goal goal = getGoalByIdAndUser(id, username);

        // Update all fields
        goal.setName(goalDetails.getName());
        goal.setCategory(goalDetails.getCategory());
        goal.setDuration(goalDetails.getDuration());
        goal.setTargetAmount(goalDetails.getTargetAmount());
        goal.setNotes(goalDetails.getNotes());
        goal.setInflationRate(goalDetails.getInflationRate());

        // Recalculate derived fields
        goal.calculateInflationAdjustedAmount();

        return goalRepository.save(goal);
    }

    public void deleteGoal(Long id, String username) {
        Goal goal = getGoalByIdAndUser(id, username);
        goalRepository.delete(goal);
    }

    @Transactional(readOnly = true)
    public List<Goal> getGoalsByCategory(String username, Goal.GoalCategory category) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return goalRepository.findByUserAndCategory(user, category);
    }

    @Transactional(readOnly = true)
    public List<Goal> getShortTermGoals(String username) {
        return getGoalsByCategory(username, Goal.GoalCategory.SHORT_TERM);
    }

    @Transactional(readOnly = true)
    public List<Goal> getMediumTermGoals(String username) {
        return getGoalsByCategory(username, Goal.GoalCategory.MEDIUM_TERM);
    }

    @Transactional(readOnly = true)
    public List<Goal> getLongTermGoals(String username) {
        return getGoalsByCategory(username, Goal.GoalCategory.LONG_TERM);
    }

    @Transactional(readOnly = true)
    public List<Goal> getRetirementGoals(String username) {
        return getGoalsByCategory(username, Goal.GoalCategory.RETIREMENT);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getGoalsSummary(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<Goal> goals = goalRepository.findByUser(user);

        double totalTargetAmount = goalRepository.findTotalTargetAmountByUser(user).orElse(0.0);

        // Count by category
        Map<Goal.GoalCategory, Long> categoryCounts = new HashMap<>();
        List<Object[]> categoryCountsResult = goalRepository.countByCategoryForUser(user);
        for (Object[] result : categoryCountsResult) {
            Goal.GoalCategory category = (Goal.GoalCategory) result[0];
            Long count = (Long) result[1];
            categoryCounts.put(category, count);
        }

        // Sum by category
        Map<Goal.GoalCategory, Double> categoryAmounts = new HashMap<>();
        List<Object[]> categoryAmountsResult = goalRepository.sumTargetAmountByCategoryForUser(user);
        for (Object[] result : categoryAmountsResult) {
            Goal.GoalCategory category = (Goal.GoalCategory) result[0];
            Double amount = (Double) result[1];
            categoryAmounts.put(category, amount);
        }

        // Calculate average duration
        double averageDuration = goals.stream()
                .mapToInt(Goal::getDuration)
                .average()
                .orElse(0.0);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalGoals", goals.size());
        summary.put("totalTargetAmount", Math.round(totalTargetAmount * 100.0) / 100.0);
        summary.put("categoryDistribution", categoryCounts);
        summary.put("categoryAmounts", categoryAmounts);
        summary.put("averageDuration", Math.round(averageDuration * 100.0) / 100.0);
        summary.put("shortTermGoals", categoryCounts.getOrDefault(Goal.GoalCategory.SHORT_TERM, 0L));
        summary.put("mediumTermGoals", categoryCounts.getOrDefault(Goal.GoalCategory.MEDIUM_TERM, 0L));
        summary.put("longTermGoals", categoryCounts.getOrDefault(Goal.GoalCategory.LONG_TERM, 0L));
        summary.put("retirementGoals", categoryCounts.getOrDefault(Goal.GoalCategory.RETIREMENT, 0L));

        return summary;
    }

    public Map<String, Object> calculateGoalPlanning(Long id, Double expectedReturn, String username) {
        Goal goal = getGoalByIdAndUser(id, username);

        Map<String, Object> planning = new HashMap<>();
        planning.put("goal", goal);
        planning.put("monthlySavingsNoReturn", goal.getMonthlySavingsRequired());
        planning.put("monthlySavingsWithReturn", goal.getMonthlySavingsWithReturn(expectedReturn));
        planning.put("totalMonths", goal.getDuration() * 12);
        planning.put("totalYears", goal.getDuration());
        planning.put("inflationAdjustedAmount", goal.getInflationAdjustedAmount());

        // Calculate yearly breakdown
        List<Map<String, Object>> yearlyBreakdown = new ArrayList<>();
        double monthlySavings = goal.getMonthlySavingsWithReturn(expectedReturn);
        double monthlyRate = expectedReturn / 12 / 100;
        double balance = 0.0;

        for (int year = 1; year <= goal.getDuration(); year++) {
            for (int month = 1; month <= 12; month++) {
                balance = (balance + monthlySavings) * (1 + monthlyRate);
            }

            Map<String, Object> yearlyData = new HashMap<>();
            yearlyData.put("year", year);
            yearlyData.put("totalContributed", Math.round(monthlySavings * 12 * year * 100.0) / 100.0);
            yearlyData.put("balance", Math.round(balance * 100.0) / 100.0);
            yearlyData.put("interestEarned", Math.round((balance - (monthlySavings * 12 * year)) * 100.0) / 100.0);

            yearlyBreakdown.add(yearlyData);
        }

        planning.put("yearlyBreakdown", yearlyBreakdown);

        return planning;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUpcomingGoals(String username, int yearsAhead) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<Goal> goals = goalRepository.findByUserAndDurationLessThanEqual(user, yearsAhead);

        return goals.stream()
                .sorted(Comparator.comparing(Goal::getDuration))
                .map(goal -> {
                    Map<String, Object> upcomingGoal = new HashMap<>();
                    upcomingGoal.put("id", goal.getId());
                    upcomingGoal.put("name", goal.getName());
                    upcomingGoal.put("category", goal.getCategory());
                    upcomingGoal.put("duration", goal.getDuration());
                    upcomingGoal.put("targetAmount", goal.getTargetAmount());
                    upcomingGoal.put("inflationAdjustedAmount", goal.getInflationAdjustedAmount());
                    upcomingGoal.put("monthlySavingsRequired", goal.getMonthlySavingsRequired());
                    upcomingGoal.put("priority", goal.getPriority());
                    return upcomingGoal;
                })
                .collect(Collectors.toList());
    }

    public Map<String, Object> trackGoalProgress(Long id, Double currentSavings, String username) {
        Goal goal = getGoalByIdAndUser(id, username);
        return goal.calculateProgress(currentSavings);
    }
}
