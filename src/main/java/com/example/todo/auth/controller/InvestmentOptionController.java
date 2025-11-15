package com.example.todo.auth.controller;

import com.example.todo.auth.entity.InvestmentOption;
import com.example.todo.auth.service.InvestmentOptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/investment-options")
public class InvestmentOptionController {

    @Autowired
    private InvestmentOptionService investmentOptionService;

    @PostMapping
    public ResponseEntity<?> createInvestmentOption(@RequestBody InvestmentOption investmentOption, Principal principal) {
        try {
            InvestmentOption savedOption = investmentOptionService.createInvestmentOption(investmentOption, principal.getName());
            return ResponseEntity.ok(savedOption);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating investment option: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<InvestmentOption>> getUserInvestmentOptions(Principal principal) {
        try {
            List<InvestmentOption> options = investmentOptionService.getUserInvestmentOptions(principal.getName());
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getInvestmentOptionById(@PathVariable Long id, Principal principal) {
        try {
            InvestmentOption option = investmentOptionService.getInvestmentOptionByIdAndUser(id, principal.getName());
            return ResponseEntity.ok(option);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateInvestmentOption(@PathVariable Long id, @RequestBody InvestmentOption investmentOption, Principal principal) {
        try {
            InvestmentOption updatedOption = investmentOptionService.updateInvestmentOption(id, investmentOption, principal.getName());
            return ResponseEntity.ok(updatedOption);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating investment option: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInvestmentOption(@PathVariable Long id, Principal principal) {
        try {
            investmentOptionService.deleteInvestmentOption(id, principal.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting investment option: " + e.getMessage());
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<?> getInvestmentOptionsByCategory(@PathVariable String category, Principal principal) {
        try {
            InvestmentOption.InvestmentCategory investmentCategory =
                    InvestmentOption.InvestmentCategory.valueOf(category.toUpperCase());
            List<InvestmentOption> options = investmentOptionService.getInvestmentOptionsByCategory(principal.getName(), investmentCategory);
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid category or error retrieving options: " + e.getMessage());
        }
    }

    @GetMapping("/risk-level/{riskLevel}")
    public ResponseEntity<?> getInvestmentOptionsByRiskLevel(@PathVariable String riskLevel, Principal principal) {
        try {
            InvestmentOption.RiskLevel risk = InvestmentOption.RiskLevel.valueOf(riskLevel.toUpperCase());
            List<InvestmentOption> options = investmentOptionService.getInvestmentOptionsByRiskLevel(principal.getName(), risk);
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid risk level or error retrieving options: " + e.getMessage());
        }
    }

    @GetMapping("/liquidity/{liquidity}")
    public ResponseEntity<?> getInvestmentOptionsByLiquidity(@PathVariable String liquidity, Principal principal) {
        try {
            InvestmentOption.Liquidity liquidityLevel = InvestmentOption.Liquidity.valueOf(liquidity.toUpperCase());
            List<InvestmentOption> options = investmentOptionService.getInvestmentOptionsByLiquidity(principal.getName(), liquidityLevel);
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid liquidity level or error retrieving options: " + e.getMessage());
        }
    }

    @GetMapping("/cagr-range")
    public ResponseEntity<?> getInvestmentOptionsByCAGRRange(
            @RequestParam Double minCAGR,
            @RequestParam Double maxCAGR,
            Principal principal) {
        try {
            List<InvestmentOption> options = investmentOptionService.getInvestmentOptionsByCAGRRange(principal.getName(), minCAGR, maxCAGR);
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving options by CAGR range: " + e.getMessage());
        }
    }

    @GetMapping("/summary/overview")
    public ResponseEntity<Map<String, Object>> getInvestmentOptionsSummary(Principal principal) {
        try {
            Map<String, Object> summary = investmentOptionService.getInvestmentOptionsSummary(principal.getName());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyMap());
        }
    }

    @GetMapping("/recommendations")
    public ResponseEntity<?> getRecommendedOptions(
            @RequestParam String preferredRiskLevel,
            @RequestParam String preferredLiquidity,
            @RequestParam Double minExpectedCAGR,
            Principal principal) {
        try {
            InvestmentOption.RiskLevel riskLevel = InvestmentOption.RiskLevel.valueOf(preferredRiskLevel.toUpperCase());
            InvestmentOption.Liquidity liquidity = InvestmentOption.Liquidity.valueOf(preferredLiquidity.toUpperCase());

            List<Map<String, Object>> recommendations = investmentOptionService.getRecommendedOptions(
                    principal.getName(), riskLevel, liquidity, minExpectedCAGR);

            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating recommendations: " + e.getMessage());
        }
    }
}
