package com.example.todo.auth.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "goals")
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private GoalCategory category;

    @Column(name = "duration", nullable = false)
    private Integer duration; // in years

    @Column(name = "target_amount", nullable = false)
    private Double targetAmount;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "inflation_adjusted_amount")
    private Double inflationAdjustedAmount;

    @Column(name = "inflation_rate")
    private Double inflationRate = 6.0; // Default inflation rate

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum GoalCategory {
        SHORT_TERM, MEDIUM_TERM, LONG_TERM, RETIREMENT
    }

    // Constructors
    public Goal() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Goal(String name, GoalCategory category, Integer duration, Double targetAmount) {
        this();
        this.name = name;
        this.category = category;
        this.duration = duration;
        this.targetAmount = targetAmount;
        calculateInflationAdjustedAmount();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GoalCategory getCategory() {
        return category;
    }

    public void setCategory(GoalCategory category) {
        this.category = category;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
        calculateInflationAdjustedAmount();
    }

    public Double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(Double targetAmount) {
        this.targetAmount = targetAmount;
        calculateInflationAdjustedAmount();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Double getInflationAdjustedAmount() {
        return inflationAdjustedAmount;
    }

    public void setInflationAdjustedAmount(Double inflationAdjustedAmount) {
        this.inflationAdjustedAmount = inflationAdjustedAmount;
    }

    public Double getInflationRate() {
        return inflationRate;
    }

    public void setInflationRate(Double inflationRate) {
        this.inflationRate = inflationRate;
        calculateInflationAdjustedAmount();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Calculation Methods
    public void calculateInflationAdjustedAmount() {
        if (targetAmount != null && duration != null && inflationRate != null) {
            // Calculate inflation adjusted amount: FV = PV * (1 + r)^n
            double adjustedAmount = targetAmount * Math.pow(1 + (inflationRate / 100), duration);
            this.inflationAdjustedAmount = Math.round(adjustedAmount * 100.0) / 100.0;
        }
    }

    // Calculate monthly savings required (assuming 0% return for simplicity)
    public Double getMonthlySavingsRequired() {
        if (targetAmount != null && duration != null) {
            double totalMonths = duration * 12.0;
            double monthlySavings = targetAmount / totalMonths;
            return Math.round(monthlySavings * 100.0) / 100.0;
        }
        return 0.0;
    }

    // Calculate monthly savings with expected return
    public Double getMonthlySavingsWithReturn(Double expectedReturn) {
        if (targetAmount != null && duration != null && expectedReturn != null) {
            double monthlyRate = expectedReturn / 12 / 100;
            double months = duration * 12.0;

            // FV = PMT * [((1 + r)^n - 1) / r]
            // PMT = FV / [((1 + r)^n - 1) / r]
            double monthlySavings = targetAmount / (((Math.pow(1 + monthlyRate, months) - 1) / monthlyRate));
            return Math.round(monthlySavings * 100.0) / 100.0;
        }
        return 0.0;
    }

    // Calculate progress based on current savings
    public Map<String, Object> calculateProgress(Double currentSavings) {
        Map<String, Object> progress = new HashMap<>();

        if (targetAmount != null && currentSavings != null) {
            double percentage = (currentSavings / targetAmount) * 100;
            double remainingAmount = targetAmount - currentSavings;

            progress.put("currentSavings", currentSavings);
            progress.put("targetAmount", targetAmount);
            progress.put("percentage", Math.round(percentage * 100.0) / 100.0);
            progress.put("remainingAmount", Math.round(remainingAmount * 100.0) / 100.0);
            progress.put("isCompleted", percentage >= 100);
        }

        return progress;
    }

    // Get category color for UI
    public String getCategoryColor() {
        switch (category) {
            case SHORT_TERM: return "info";
            case MEDIUM_TERM: return "warning";
            case LONG_TERM: return "primary";
            case RETIREMENT: return "success";
            default: return "secondary";
        }
    }

    // Get category icon for UI
    public String getCategoryIcon() {
        switch (category) {
            case SHORT_TERM: return "fa-bolt";
            case MEDIUM_TERM: return "fa-chart-line";
            case LONG_TERM: return "fa-mountain";
            case RETIREMENT: return "fa-umbrella-beach";
            default: return "fa-bullseye";
        }
    }

    // Get priority based on category and duration
    public String getPriority() {
        switch (category) {
            case SHORT_TERM: return "HIGH";
            case MEDIUM_TERM: return "MEDIUM";
            case LONG_TERM: return "LOW";
            case RETIREMENT: return "VERY_HIGH";
            default: return "MEDIUM";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateInflationAdjustedAmount();
    }

    @Override
    public String toString() {
        return "Goal{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", duration=" + duration +
                ", targetAmount=" + targetAmount +
                ", inflationAdjustedAmount=" + inflationAdjustedAmount +
                '}';
    }
}
