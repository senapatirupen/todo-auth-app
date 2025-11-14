package com.example.todo.auth.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "emis")
public class EMI {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emi_for_name", nullable = false)
    private String emiForName;

    @Column(name = "principal", nullable = false)
    private Double principal;

    @Column(name = "annual_interest_rate", nullable = false)
    private Double annualInterestRate;

    @Column(name = "total_tenure", nullable = false)
    private Integer totalTenure;

    @Column(name = "tenures_paid", nullable = false)
    private Integer tenuresPaid = 0;

    @Column(name = "emi_amount")
    private Double emiAmount;

    @Column(name = "principal_paid_so_far")
    private Double principalPaidSoFar = 0.0;

    @Column(name = "interest_paid_so_far")
    private Double interestPaidSoFar = 0.0;

    @Column(name = "remaining_principal")
    private Double remainingPrincipal;

    @Column(name = "interest_to_be_paid")
    private Double interestToBePaid;

    @Column(name = "remaining_tenure")
    private Integer remainingTenure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public EMI() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public EMI(String emiForName, Double principal, Double annualInterestRate, Integer totalTenure) {
        this();
        this.emiForName = emiForName;
        this.principal = principal;
        this.annualInterestRate = annualInterestRate;
        this.totalTenure = totalTenure;
        calculateEMI();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmiForName() {
        return emiForName;
    }

    public void setEmiForName(String emiForName) {
        this.emiForName = emiForName;
    }

    public Double getPrincipal() {
        return principal;
    }

    public void setPrincipal(Double principal) {
        this.principal = principal;
        calculateEMI();
    }

    public Double getAnnualInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(Double annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
        calculateEMI();
    }

    public Integer getTotalTenure() {
        return totalTenure;
    }

    public void setTotalTenure(Integer totalTenure) {
        this.totalTenure = totalTenure;
        calculateEMI();
    }

    public Integer getTenuresPaid() {
        return tenuresPaid;
    }

    public void setTenuresPaid(Integer tenuresPaid) {
        this.tenuresPaid = tenuresPaid;
        calculateRemainingDetails();
    }

    public Double getEmiAmount() {
        return emiAmount;
    }

    public void setEmiAmount(Double emiAmount) {
        this.emiAmount = emiAmount;
    }

    public Double getPrincipalPaidSoFar() {
        return principalPaidSoFar;
    }

    public void setPrincipalPaidSoFar(Double principalPaidSoFar) {
        this.principalPaidSoFar = principalPaidSoFar;
    }

    public Double getInterestPaidSoFar() {
        return interestPaidSoFar;
    }

    public void setInterestPaidSoFar(Double interestPaidSoFar) {
        this.interestPaidSoFar = interestPaidSoFar;
    }

    public Double getRemainingPrincipal() {
        return remainingPrincipal;
    }

    public void setRemainingPrincipal(Double remainingPrincipal) {
        this.remainingPrincipal = remainingPrincipal;
    }

    public Double getInterestToBePaid() {
        return interestToBePaid;
    }

    public void setInterestToBePaid(Double interestToBePaid) {
        this.interestToBePaid = interestToBePaid;
    }

    public Integer getRemainingTenure() {
        return remainingTenure;
    }

    public void setRemainingTenure(Integer remainingTenure) {
        this.remainingTenure = remainingTenure;
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

    // EMI Calculation Methods
    public void calculateEMI() {
        if (principal != null && annualInterestRate != null && totalTenure != null) {
            double monthlyInterestRate = annualInterestRate / 12 / 100;
            double emi = principal * monthlyInterestRate *
                    Math.pow(1 + monthlyInterestRate, totalTenure) /
                    (Math.pow(1 + monthlyInterestRate, totalTenure) - 1);
            this.emiAmount = Math.round(emi * 100.0) / 100.0;
            calculateRemainingDetails();
        }
    }

    public void calculateRemainingDetails() {
        if (principal != null && annualInterestRate != null && totalTenure != null && tenuresPaid != null) {
            double monthlyInterestRate = annualInterestRate / 12 / 100;
            double remainingPrincipal = principal;
            double totalInterestPaid = 0.0;
            double totalPrincipalPaid = 0.0;

            // Calculate paid amounts
            for (int i = 0; i < tenuresPaid; i++) {
                double interestComponent = remainingPrincipal * monthlyInterestRate;
                double principalComponent = emiAmount - interestComponent;

                totalInterestPaid += interestComponent;
                totalPrincipalPaid += principalComponent;
                remainingPrincipal -= principalComponent;
            }

            this.principalPaidSoFar = Math.round(totalPrincipalPaid * 100.0) / 100.0;
            this.interestPaidSoFar = Math.round(totalInterestPaid * 100.0) / 100.0;
            this.remainingPrincipal = Math.round(remainingPrincipal * 100.0) / 100.0;
            this.remainingTenure = totalTenure - tenuresPaid;

            // Calculate remaining interest
            double remainingInterest = 0.0;
            double tempPrincipal = remainingPrincipal;
            for (int i = 0; i < remainingTenure; i++) {
                double interestComponent = tempPrincipal * monthlyInterestRate;
                double principalComponent = emiAmount - interestComponent;
                remainingInterest += interestComponent;
                tempPrincipal -= principalComponent;
            }

            this.interestToBePaid = Math.round(remainingInterest * 100.0) / 100.0;
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void makePayment() {
        if (tenuresPaid < totalTenure) {
            this.tenuresPaid++;
            calculateRemainingDetails();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "EMI{" +
                "id=" + id +
                ", emiForName='" + emiForName + '\'' +
                ", principal=" + principal +
                ", annualInterestRate=" + annualInterestRate +
                ", totalTenure=" + totalTenure +
                ", tenuresPaid=" + tenuresPaid +
                ", emiAmount=" + emiAmount +
                ", remainingPrincipal=" + remainingPrincipal +
                '}';
    }
}
