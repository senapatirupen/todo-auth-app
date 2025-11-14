package com.example.todo.auth.controller;

import com.example.todo.auth.entity.SIP;
import com.example.todo.auth.service.SIPService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sips")
public class SIPController {

    @Autowired
    private SIPService sipService;

    @PostMapping
    public ResponseEntity<?> createSIP(@RequestBody SIP sip, Principal principal) {
        try {
            SIP savedSIP = sipService.createSIP(sip, principal.getName());
            return ResponseEntity.ok(savedSIP);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating SIP: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<SIP>> getUserSIPs(Principal principal) {
        try {
            List<SIP> sips = sipService.getUserSIPs(principal.getName());
            return ResponseEntity.ok(sips);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSIPById(@PathVariable Long id, Principal principal) {
        try {
            SIP sip = sipService.getSIPByIdAndUser(id, principal.getName());
            return ResponseEntity.ok(sip);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSIP(@PathVariable Long id, @RequestBody SIP sip, Principal principal) {
        try {
            SIP updatedSIP = sipService.updateSIP(id, sip, principal.getName());
            return ResponseEntity.ok(updatedSIP);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating SIP: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSIP(@PathVariable Long id, Principal principal) {
        try {
            sipService.deleteSIP(id, principal.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting SIP: " + e.getMessage());
        }
    }

    @GetMapping("/summary/total-monthly")
    public ResponseEntity<Double> getTotalMonthlyInvestment(Principal principal) {
        try {
            Double totalMonthlyInvestment = sipService.getTotalMonthlyInvestment(principal.getName());
            return ResponseEntity.ok(totalMonthlyInvestment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(0.0);
        }
    }

    @GetMapping("/summary/overview")
    public ResponseEntity<Map<String, Object>> getSIPSummary(Principal principal) {
        try {
            Map<String, Object> summary = sipService.getSIPSummary(principal.getName());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyMap());
        }
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<?> getSIPProgress(
            @PathVariable Long id,
            @RequestParam Integer monthsCompleted,
            Principal principal) {
        try {
            Map<String, Object> progress = sipService.calculateSIPProgress(id, monthsCompleted, principal.getName());
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error calculating progress: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/projection")
    public ResponseEntity<?> getSIPProjection(@PathVariable Long id, Principal principal) {
        try {
            List<Map<String, Object>> projection = sipService.getSIPProjection(id, principal.getName());
            return ResponseEntity.ok(projection);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating projection: " + e.getMessage());
        }
    }
}
