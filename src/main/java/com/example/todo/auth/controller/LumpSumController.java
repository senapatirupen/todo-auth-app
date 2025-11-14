package com.example.todo.auth.controller;

import com.example.todo.auth.entity.LumpSum;
import com.example.todo.auth.service.LumpSumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lump-sums")
public class LumpSumController {

    @Autowired
    private LumpSumService lumpSumService;

    @PostMapping
    public ResponseEntity<?> createLumpSum(@RequestBody LumpSum lumpSum, Principal principal) {
        try {
            LumpSum savedLumpSum = lumpSumService.createLumpSum(lumpSum, principal.getName());
            return ResponseEntity.ok(savedLumpSum);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating LumpSum investment: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<LumpSum>> getUserLumpSums(Principal principal) {
        try {
            List<LumpSum> lumpSums = lumpSumService.getUserLumpSums(principal.getName());
            return ResponseEntity.ok(lumpSums);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLumpSumById(@PathVariable Long id, Principal principal) {
        try {
            LumpSum lumpSum = lumpSumService.getLumpSumByIdAndUser(id, principal.getName());
            return ResponseEntity.ok(lumpSum);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLumpSum(@PathVariable Long id, @RequestBody LumpSum lumpSum, Principal principal) {
        try {
            LumpSum updatedLumpSum = lumpSumService.updateLumpSum(id, lumpSum, principal.getName());
            return ResponseEntity.ok(updatedLumpSum);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating LumpSum investment: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLumpSum(@PathVariable Long id, Principal principal) {
        try {
            lumpSumService.deleteLumpSum(id, principal.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting LumpSum investment: " + e.getMessage());
        }
    }

    @GetMapping("/summary/overview")
    public ResponseEntity<Map<String, Object>> getLumpSumSummary(Principal principal) {
        try {
            Map<String, Object> summary = lumpSumService.getLumpSumSummary(principal.getName());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyMap());
        }
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<?> getLumpSumProgress(
            @PathVariable Long id,
            @RequestParam Integer yearsCompleted,
            Principal principal) {
        try {
            Map<String, Object> progress = lumpSumService.calculateLumpSumProgress(id, yearsCompleted, principal.getName());
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error calculating progress: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/projection")
    public ResponseEntity<?> getLumpSumProjection(@PathVariable Long id, Principal principal) {
        try {
            List<Map<String, Object>> projection = lumpSumService.getLumpSumProjection(id, principal.getName());
            return ResponseEntity.ok(projection);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating projection: " + e.getMessage());
        }
    }

    @GetMapping("/top-performing")
    public ResponseEntity<?> getTopPerformingInvestments(
            @RequestParam(defaultValue = "5") int limit,
            Principal principal) {
        try {
            List<Map<String, Object>> topPerformers = lumpSumService.getTopPerformingInvestments(principal.getName(), limit);
            return ResponseEntity.ok(topPerformers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error getting top performers: " + e.getMessage());
        }
    }
}
