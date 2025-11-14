package com.example.todo.auth.service;

import com.example.todo.auth.entity.EMI;
import com.example.todo.auth.entity.User;
import com.example.todo.auth.repository.EMIRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class EMIService {

    @Autowired
    private EMIRepository emiRepository;

    @Autowired
    private UserService userService;

    public EMI createEMI(EMI emi, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        emi.setUser(user);
        emi.calculateEMI(); // Calculate all derived fields
        return emiRepository.save(emi);
    }

    @Transactional(readOnly = true)
    public List<EMI> getUserEMIs(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return emiRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public EMI getEMIByIdAndUser(Long id, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return emiRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("EMI not found with id: " + id));
    }

    public EMI updateEMI(Long id, EMI emiDetails, String username) {
        EMI emi = getEMIByIdAndUser(id, username);

        // Update basic fields
        emi.setEmiForName(emiDetails.getEmiForName());
        emi.setPrincipal(emiDetails.getPrincipal());
        emi.setAnnualInterestRate(emiDetails.getAnnualInterestRate());
        emi.setTotalTenure(emiDetails.getTotalTenure());
        emi.setTenuresPaid(emiDetails.getTenuresPaid());

        // Recalculate all derived fields
        emi.calculateEMI();

        return emiRepository.save(emi);
    }

    public void deleteEMI(Long id, String username) {
        EMI emi = getEMIByIdAndUser(id, username);
        emiRepository.delete(emi);
    }

    public EMI makePayment(Long id, String username) {
        EMI emi = getEMIByIdAndUser(id, username);
        emi.makePayment();
        return emiRepository.save(emi);
    }

    @Transactional(readOnly = true)
    public Double getTotalMonthlyEMI(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return emiRepository.findTotalMonthlyEMIByUser(user).orElse(0.0);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getEMISummary(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<EMI> emis = emiRepository.findByUser(user);

        double totalPrincipal = emis.stream().mapToDouble(EMI::getPrincipal).sum();
        double totalEMI = emis.stream().mapToDouble(EMI::getEmiAmount).sum();
        double totalPaid = emis.stream().mapToDouble(emi -> emi.getPrincipalPaidSoFar() + emi.getInterestPaidSoFar()).sum();
        double remainingPrincipal = emis.stream().mapToDouble(EMI::getRemainingPrincipal).sum();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalEMIs", emis.size());
        summary.put("totalPrincipal", Math.round(totalPrincipal * 100.0) / 100.0);
        summary.put("totalMonthlyEMI", Math.round(totalEMI * 100.0) / 100.0);
        summary.put("totalPaid", Math.round(totalPaid * 100.0) / 100.0);
        summary.put("remainingPrincipal", Math.round(remainingPrincipal * 100.0) / 100.0);

        return summary;
    }
}
