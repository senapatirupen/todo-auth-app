package com.example.todo.auth.controller;

import com.example.todo.auth.entity.EMI;
import com.example.todo.auth.service.EMIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emis")
public class EMIController {

    @Autowired
    private EMIService emiService;

    @PostMapping
    public ResponseEntity<?> createEMI(@RequestBody EMI emi, Principal principal) {
        try {
            EMI savedEMI = emiService.createEMI(emi, principal.getName());
            return ResponseEntity.ok(savedEMI);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating EMI: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<EMI>> getUserEMIs(Principal principal) {
        try {
            List<EMI> emis = emiService.getUserEMIs(principal.getName());
            return ResponseEntity.ok(emis);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEMIById(@PathVariable Long id, Principal principal) {
        try {
            EMI emi = emiService.getEMIByIdAndUser(id, principal.getName());
            return ResponseEntity.ok(emi);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEMI(@PathVariable Long id, @RequestBody EMI emi, Principal principal) {
        try {
            EMI updatedEMI = emiService.updateEMI(id, emi, principal.getName());
            return ResponseEntity.ok(updatedEMI);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating EMI: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEMI(@PathVariable Long id, Principal principal) {
        try {
            emiService.deleteEMI(id, principal.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting EMI: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/payment")
    public ResponseEntity<?> makePayment(@PathVariable Long id, Principal principal) {
        try {
            EMI updatedEMI = emiService.makePayment(id, principal.getName());
            return ResponseEntity.ok(updatedEMI);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing payment: " + e.getMessage());
        }
    }

    @GetMapping("/summary/total-monthly")
    public ResponseEntity<Double> getTotalMonthlyEMI(Principal principal) {
        try {
            Double totalMonthlyEMI = emiService.getTotalMonthlyEMI(principal.getName());
            return ResponseEntity.ok(totalMonthlyEMI);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(0.0);
        }
    }

    @GetMapping("/summary/overview")
    public ResponseEntity<Map<String, Object>> getEMISummary(Principal principal) {
        try {
            Map<String, Object> summary = emiService.getEMISummary(principal.getName());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyMap());
        }
    }
}