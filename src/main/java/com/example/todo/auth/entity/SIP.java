package com.example.todo.auth.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "sips")
public class SIP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "investment_on_name", nullable = false)
    private String investmentOnName;

    @Column(name = "monthly_investment", nullable = false)
    private Double monthlyInvestment;

    @Column(name = "duration", nullable = false)
    private Integer duration; // in months

    @Column(name = "expected_return", nullable = false)
    private Double expectedReturn; // annual expected return percentage

    @Column(name = "future_value")
    private Double futureValue;

    @Column(name = "total_investment")
    private Double totalInvestment;

    @Column(name = "total_interest_paid")
    private Double totalInterestPaid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public SIP() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public SIP(String investmentOnName, Double monthlyInvestment, Integer duration, Double expectedReturn) {
        this();
        this.investmentOnName = investmentOnName;
        this.monthlyInvestment = monthlyInvestment;
        this.duration = duration;
        this.expectedReturn = expectedReturn;
        calculateSIP();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvestmentOnName() {
        return investmentOnName;
    }

    public void setInvestmentOnName(String investmentOnName) {
        this.investmentOnName = investmentOnName;
    }

    public Double getMonthlyInvestment() {
        return monthlyInvestment;
    }

    public void setMonthlyInvestment(Double monthlyInvestment) {
        this.monthlyInvestment = monthlyInvestment;
        calculateSIP();
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
        calculateSIP();
    }

    public Double getExpectedReturn() {
        return expectedReturn;
    }

    public void setExpectedReturn(Double expectedReturn) {
        this.expectedReturn = expectedReturn;
        calculateSIP();
    }

    public Double getFutureValue() {
        return futureValue;
    }

    public void setFutureValue(Double futureValue) {
        this.futureValue = futureValue;
    }

    public Double getTotalInvestment() {
        return totalInvestment;
    }

    public void setTotalInvestment(Double totalInvestment) {
        this.totalInvestment = totalInvestment;
    }

    public Double getTotalInterestPaid() {
        return totalInterestPaid;
    }

    public void setTotalInterestPaid(Double totalInterestPaid) {
        this.totalInterestPaid = totalInterestPaid;
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

    // SIP Calculation Methods
    public void calculateSIP() {
        if (monthlyInvestment != null && duration != null && expectedReturn != null) {
            // Calculate future value using SIP formula: FV = P * [((1 + r)^n - 1) / r] * (1 + r)
            // where P = monthly investment, r = monthly rate, n = number of months
            double monthlyRate = expectedReturn / 12 / 100;
            double months = duration.doubleValue();

            // Future value formula for SIP
            double fv = monthlyInvestment *
                    (Math.pow(1 + monthlyRate, months) - 1) / monthlyRate *
                    (1 + monthlyRate);

            this.futureValue = Math.round(fv * 100.0) / 100.0;
            this.totalInvestment = Math.round(monthlyInvestment * months * 100.0) / 100.0;
            this.totalInterestPaid = Math.round((futureValue - totalInvestment) * 100.0) / 100.0;
            this.updatedAt = LocalDateTime.now();
        }
    }

    // Calculate values for a specific period (for progress tracking)
    public Map<String, Double> calculateForPeriod(int monthsCompleted) {
        if (monthlyInvestment != null && expectedReturn != null && monthsCompleted <= duration) {
            double monthlyRate = expectedReturn / 12 / 100;

            // Current value after n months
            double currentValue = monthlyInvestment *
                    (Math.pow(1 + monthlyRate, monthsCompleted) - 1) / monthlyRate *
                    (1 + monthlyRate);

            double investedSoFar = monthlyInvestment * monthsCompleted;
            double interestSoFar = currentValue - investedSoFar;

            Map<String, Double> result = new HashMap<>();
            result.put("currentValue", Math.round(currentValue * 100.0) / 100.0);
            result.put("investedSoFar", Math.round(investedSoFar * 100.0) / 100.0);
            result.put("interestSoFar", Math.round(interestSoFar * 100.0) / 100.0);
            result.put("remainingMonths", (double) (duration - monthsCompleted));

            return result;
        }
        return Collections.emptyMap();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "SIP{" +
                "id=" + id +
                ", investmentOnName='" + investmentOnName + '\'' +
                ", monthlyInvestment=" + monthlyInvestment +
                ", duration=" + duration +
                ", expectedReturn=" + expectedReturn +
                ", futureValue=" + futureValue +
                ", totalInvestment=" + totalInvestment +
                ", totalInterestPaid=" + totalInterestPaid +
                '}';
    }
}
