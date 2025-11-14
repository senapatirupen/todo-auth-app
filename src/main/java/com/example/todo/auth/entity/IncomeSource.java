package com.example.todo.auth.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "income_sources")
public class IncomeSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_name", nullable = false)
    private String sourceName;

    @Column(name = "initial_monthly_income", nullable = false)
    private Double initialMonthlyIncome;

    @Column(name = "annual_growth_rate", nullable = false)
    private Double annualGrowthRate; // percentage

    @Column(name = "years", nullable = false)
    private Integer years;

    @Column(name = "projected_monthly_income")
    private Double projectedMonthlyIncome;

    @Column(name = "total_amount_received")
    private Double totalAmountReceived;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public IncomeSource() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public IncomeSource(String sourceName, Double initialMonthlyIncome, Double annualGrowthRate, Integer years) {
        this();
        this.sourceName = sourceName;
        this.initialMonthlyIncome = initialMonthlyIncome;
        this.annualGrowthRate = annualGrowthRate;
        this.years = years;
        calculateIncomeProjection();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public Double getInitialMonthlyIncome() {
        return initialMonthlyIncome;
    }

    public void setInitialMonthlyIncome(Double initialMonthlyIncome) {
        this.initialMonthlyIncome = initialMonthlyIncome;
        calculateIncomeProjection();
    }

    public Double getAnnualGrowthRate() {
        return annualGrowthRate;
    }

    public void setAnnualGrowthRate(Double annualGrowthRate) {
        this.annualGrowthRate = annualGrowthRate;
        calculateIncomeProjection();
    }

    public Integer getYears() {
        return years;
    }

    public void setYears(Integer years) {
        this.years = years;
        calculateIncomeProjection();
    }

    public Double getProjectedMonthlyIncome() {
        return projectedMonthlyIncome;
    }

    public void setProjectedMonthlyIncome(Double projectedMonthlyIncome) {
        this.projectedMonthlyIncome = projectedMonthlyIncome;
    }

    public Double getTotalAmountReceived() {
        return totalAmountReceived;
    }

    public void setTotalAmountReceived(Double totalAmountReceived) {
        this.totalAmountReceived = totalAmountReceived;
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

    // Income Projection Calculation Methods
    public void calculateIncomeProjection() {
        if (initialMonthlyIncome != null && annualGrowthRate != null && years != null) {
            // Calculate projected monthly income after n years with annual growth
            double projectedIncome = initialMonthlyIncome * Math.pow(1 + (annualGrowthRate / 100), years);
            this.projectedMonthlyIncome = Math.round(projectedIncome * 100.0) / 100.0;

            // Calculate total amount received over the years
            double totalAmount = 0.0;
            double currentYearIncome = initialMonthlyIncome * 12; // Annual income for first year

            for (int year = 1; year <= years; year++) {
                totalAmount += currentYearIncome;
                currentYearIncome *= (1 + (annualGrowthRate / 100)); // Grow income for next year
            }

            this.totalAmountReceived = Math.round(totalAmount * 100.0) / 100.0;
            this.updatedAt = LocalDateTime.now();
        }
    }

    // Calculate year-by-year income progression
    public List<Map<String, Object>> getYearlyIncomeProgression() {
        List<Map<String, Object>> progression = new ArrayList<>();

        double currentMonthlyIncome = initialMonthlyIncome;

        for (int year = 1; year <= years; year++) {
            double annualIncome = currentMonthlyIncome * 12;
            double cumulativeIncome = 0.0;

            // Calculate cumulative income up to this year
            double tempMonthlyIncome = initialMonthlyIncome;
            for (int y = 1; y <= year; y++) {
                cumulativeIncome += tempMonthlyIncome * 12;
                tempMonthlyIncome *= (1 + (annualGrowthRate / 100));
            }

            Map<String, Object> yearlyData = new HashMap<>();
            yearlyData.put("year", year);
            yearlyData.put("monthlyIncome", Math.round(currentMonthlyIncome * 100.0) / 100.0);
            yearlyData.put("annualIncome", Math.round(annualIncome * 100.0) / 100.0);
            yearlyData.put("cumulativeIncome", Math.round(cumulativeIncome * 100.0) / 100.0);
            yearlyData.put("growthFromStart", Math.round(((currentMonthlyIncome - initialMonthlyIncome) / initialMonthlyIncome) * 100 * 100.0) / 100.0);

            progression.add(yearlyData);

            // Grow income for next year
            currentMonthlyIncome *= (1 + (annualGrowthRate / 100));
        }

        return progression;
    }

    // Calculate income for a specific year
    public Map<String, Object> calculateIncomeForYear(int targetYear) {
        if (targetYear < 1 || targetYear > years) {
            throw new IllegalArgumentException("Target year must be between 1 and " + years);
        }

        double monthlyIncome = initialMonthlyIncome * Math.pow(1 + (annualGrowthRate / 100), targetYear - 1);
        double annualIncome = monthlyIncome * 12;

        Map<String, Object> result = new HashMap<>();
        result.put("year", targetYear);
        result.put("monthlyIncome", Math.round(monthlyIncome * 100.0) / 100.0);
        result.put("annualIncome", Math.round(annualIncome * 100.0) / 100.0);
        result.put("growthFromPrevious", targetYear > 1 ?
                Math.round((annualGrowthRate * 100.0) / 100.0) : 0.0);

        return result;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "IncomeSource{" +
                "id=" + id +
                ", sourceName='" + sourceName + '\'' +
                ", initialMonthlyIncome=" + initialMonthlyIncome +
                ", annualGrowthRate=" + annualGrowthRate +
                ", years=" + years +
                ", projectedMonthlyIncome=" + projectedMonthlyIncome +
                ", totalAmountReceived=" + totalAmountReceived +
                '}';
    }
}
