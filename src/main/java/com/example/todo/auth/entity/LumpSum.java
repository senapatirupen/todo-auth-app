package com.example.todo.auth.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "lump_sums")
public class LumpSum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "investment_name", nullable = false)
    private String investmentName;

    @Column(name = "principal_amount", nullable = false)
    private Double principalAmount;

    @Column(name = "duration", nullable = false)
    private Integer duration; // in years

    @Column(name = "expected_return", nullable = false)
    private Double expectedReturn; // annual expected return percentage

    @Column(name = "future_value")
    private Double futureValue;

    @Column(name = "total_interest")
    private Double totalInterest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public LumpSum() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public LumpSum(String investmentName, Double principalAmount, Integer duration, Double expectedReturn) {
        this();
        this.investmentName = investmentName;
        this.principalAmount = principalAmount;
        this.duration = duration;
        this.expectedReturn = expectedReturn;
        calculateLumpSum();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvestmentName() {
        return investmentName;
    }

    public void setInvestmentName(String investmentName) {
        this.investmentName = investmentName;
    }

    public Double getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(Double principalAmount) {
        this.principalAmount = principalAmount;
        calculateLumpSum();
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
        calculateLumpSum();
    }

    public Double getExpectedReturn() {
        return expectedReturn;
    }

    public void setExpectedReturn(Double expectedReturn) {
        this.expectedReturn = expectedReturn;
        calculateLumpSum();
    }

    public Double getFutureValue() {
        return futureValue;
    }

    public void setFutureValue(Double futureValue) {
        this.futureValue = futureValue;
    }

    public Double getTotalInterest() {
        return totalInterest;
    }

    public void setTotalInterest(Double totalInterest) {
        this.totalInterest = totalInterest;
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

    // LumpSum Calculation Methods
    public void calculateLumpSum() {
        if (principalAmount != null && duration != null && expectedReturn != null) {
            // Calculate future value using compound interest formula: FV = P * (1 + r)^n
            // where P = principal amount, r = annual rate, n = number of years
            double futureVal = principalAmount * Math.pow(1 + (expectedReturn / 100), duration);

            this.futureValue = Math.round(futureVal * 100.0) / 100.0;
            this.totalInterest = Math.round((futureValue - principalAmount) * 100.0) / 100.0;
            this.updatedAt = LocalDateTime.now();
        }
    }

    // Calculate values for a specific period (for progress tracking)
    public Map<String, Double> calculateForPeriod(int yearsCompleted) {
        if (principalAmount != null && expectedReturn != null && yearsCompleted <= duration) {
            // Current value after n years
            double currentValue = principalAmount * Math.pow(1 + (expectedReturn / 100), yearsCompleted);
            double interestSoFar = currentValue - principalAmount;
            double remainingYears = duration - yearsCompleted;

            // Projected future value from current position
            double projectedFutureValue = currentValue * Math.pow(1 + (expectedReturn / 100), remainingYears);

            Map<String, Double> result = new HashMap<>();
            result.put("currentValue", Math.round(currentValue * 100.0) / 100.0);
            result.put("interestSoFar", Math.round(interestSoFar * 100.0) / 100.0);
            result.put("remainingYears", (double) remainingYears);
            result.put("projectedFutureValue", Math.round(projectedFutureValue * 100.0) / 100.0);

            return result;
        }
        return Collections.emptyMap();
    }

    // Calculate year-by-year growth projection
    public List<Map<String, Object>> getYearlyProjection() {
        List<Map<String, Object>> projection = new ArrayList<>();

        for (int year = 0; year <= duration; year++) {
            double value = principalAmount * Math.pow(1 + (expectedReturn / 100), year);
            double interest = value - principalAmount;

            Map<String, Object> yearlyData = new HashMap<>();
            yearlyData.put("year", year);
            yearlyData.put("principal", Math.round(principalAmount * 100.0) / 100.0);
            yearlyData.put("interest", Math.round(interest * 100.0) / 100.0);
            yearlyData.put("totalValue", Math.round(value * 100.0) / 100.0);
            yearlyData.put("returnPercentage", Math.round((interest / principalAmount) * 100 * 100.0) / 100.0);

            projection.add(yearlyData);
        }

        return projection;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "LumpSum{" +
                "id=" + id +
                ", investmentName='" + investmentName + '\'' +
                ", principalAmount=" + principalAmount +
                ", duration=" + duration +
                ", expectedReturn=" + expectedReturn +
                ", futureValue=" + futureValue +
                ", totalInterest=" + totalInterest +
                '}';
    }
}
