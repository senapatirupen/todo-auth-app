package com.example.todo.auth.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "investment_options")
public class InvestmentOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private InvestmentCategory category;

    @Column(name = "min_cagr", nullable = false)
    private Double minCAGR;

    @Column(name = "max_cagr", nullable = false)
    private Double maxCAGR;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "liquidity", nullable = false, length = 20)
    private Liquidity liquidity;

    @Column(name = "tax_efficiency", length = 500)
    private String taxEfficiency;

    @Column(name = "notes", length = 1000)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Enums
    public enum InvestmentCategory {
        EQUITY, FIXED_INCOME, REAL_ESTATE, COMMODITIES, ALTERNATIVE
    }

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, VERY_HIGH
    }

    public enum Liquidity {
        HIGH, MEDIUM, LOW
    }

    // Constructors
    public InvestmentOption() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public InvestmentOption(String name, InvestmentCategory category, Double minCAGR, Double maxCAGR,
                            RiskLevel riskLevel, Liquidity liquidity) {
        this();
        this.name = name;
        this.category = category;
        this.minCAGR = minCAGR;
        this.maxCAGR = maxCAGR;
        this.riskLevel = riskLevel;
        this.liquidity = liquidity;
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

    public InvestmentCategory getCategory() {
        return category;
    }

    public void setCategory(InvestmentCategory category) {
        this.category = category;
    }

    public Double getMinCAGR() {
        return minCAGR;
    }

    public void setMinCAGR(Double minCAGR) {
        this.minCAGR = minCAGR;
    }

    public Double getMaxCAGR() {
        return maxCAGR;
    }

    public void setMaxCAGR(Double maxCAGR) {
        this.maxCAGR = maxCAGR;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Liquidity getLiquidity() {
        return liquidity;
    }

    public void setLiquidity(Liquidity liquidity) {
        this.liquidity = liquidity;
    }

    public String getTaxEfficiency() {
        return taxEfficiency;
    }

    public void setTaxEfficiency(String taxEfficiency) {
        this.taxEfficiency = taxEfficiency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    // Helper methods
    public Double getAverageCAGR() {
        return (minCAGR + maxCAGR) / 2;
    }

    public String getRiskColor() {
        switch (riskLevel) {
            case LOW: return "success";
            case MEDIUM: return "warning";
            case HIGH: return "danger";
            case VERY_HIGH: return "dark";
            default: return "secondary";
        }
    }

    public String getLiquidityIcon() {
        switch (liquidity) {
            case HIGH: return "fa-tachometer-alt-fast";
            case MEDIUM: return "fa-tachometer-alt-average";
            case LOW: return "fa-tachometer-alt-slow";
            default: return "fa-tachometer-alt";
        }
    }

    public String getCategoryIcon() {
        switch (category) {
            case EQUITY: return "fa-chart-line";
            case FIXED_INCOME: return "fa-hand-holding-usd";
            case REAL_ESTATE: return "fa-home";
            case COMMODITIES: return "fa-gem";
            case ALTERNATIVE: return "fa-lightbulb";
            default: return "fa-chart-pie";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "InvestmentOption{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", minCAGR=" + minCAGR +
                ", maxCAGR=" + maxCAGR +
                ", riskLevel=" + riskLevel +
                ", liquidity=" + liquidity +
                ", taxEfficiency='" + taxEfficiency + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}
