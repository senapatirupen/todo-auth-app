package com.example.todo.auth.service;

import com.example.todo.auth.entity.SIP;
import com.example.todo.auth.entity.User;
import com.example.todo.auth.repository.SIPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SIPService {

    @Autowired
    private SIPRepository sipRepository;

    @Autowired
    private UserService userService;

    public SIP createSIP(SIP sip, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        sip.setUser(user);
        sip.calculateSIP(); // Calculate all derived fields
        return sipRepository.save(sip);
    }

    @Transactional(readOnly = true)
    public List<SIP> getUserSIPs(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return sipRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public SIP getSIPByIdAndUser(Long id, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return sipRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("SIP not found with id: " + id));
    }

    public SIP updateSIP(Long id, SIP sipDetails, String username) {
        SIP sip = getSIPByIdAndUser(id, username);

        // Update basic fields
        sip.setInvestmentOnName(sipDetails.getInvestmentOnName());
        sip.setMonthlyInvestment(sipDetails.getMonthlyInvestment());
        sip.setDuration(sipDetails.getDuration());
        sip.setExpectedReturn(sipDetails.getExpectedReturn());

        // Recalculate all derived fields
        sip.calculateSIP();

        return sipRepository.save(sip);
    }

    public void deleteSIP(Long id, String username) {
        SIP sip = getSIPByIdAndUser(id, username);
        sipRepository.delete(sip);
    }

    @Transactional(readOnly = true)
    public Double getTotalMonthlyInvestment(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return sipRepository.findTotalMonthlyInvestmentByUser(user).orElse(0.0);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSIPSummary(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<SIP> sips = sipRepository.findByUser(user);

        double totalMonthlyInvestment = sips.stream().mapToDouble(SIP::getMonthlyInvestment).sum();
        double totalFutureValue = sips.stream().mapToDouble(SIP::getFutureValue).sum();
        double totalInvestment = sips.stream().mapToDouble(SIP::getTotalInvestment).sum();
        double totalExpectedInterest = sips.stream().mapToDouble(SIP::getTotalInterestPaid).sum();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalSIPs", sips.size());
        summary.put("totalMonthlyInvestment", Math.round(totalMonthlyInvestment * 100.0) / 100.0);
        summary.put("totalFutureValue", Math.round(totalFutureValue * 100.0) / 100.0);
        summary.put("totalInvestment", Math.round(totalInvestment * 100.0) / 100.0);
        summary.put("totalExpectedInterest", Math.round(totalExpectedInterest * 100.0) / 100.0);
        summary.put("averageReturn", sips.isEmpty() ? 0.0 : Math.round(sips.stream().mapToDouble(SIP::getExpectedReturn).average().orElse(0.0) * 100.0) / 100.0);

        return summary;
    }

    public Map<String, Object> calculateSIPProgress(Long id, Integer monthsCompleted, String username) {
        SIP sip = getSIPByIdAndUser(id, username);
//        return sip.calculateForPeriod(monthsCompleted);
        return null;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSIPProjection(Long id, String username) {
        SIP sip = getSIPByIdAndUser(id, username);
        List<Map<String, Object>> projection = new ArrayList<>();

        for (int month = 1; month <= sip.getDuration(); month++) {
            Map<String, Double> monthlyData = sip.calculateForPeriod(month);
            Map<String, Object> projectionData = new HashMap<>();
            projectionData.put("month", month);
            projectionData.put("monthlyInvestment", sip.getMonthlyInvestment());
            projectionData.put("cumulativeInvestment", monthlyData.get("investedSoFar"));
            projectionData.put("interestEarned", monthlyData.get("interestSoFar"));
            projectionData.put("totalValue", monthlyData.get("currentValue"));
            projection.add(projectionData);
        }

        return projection;
    }
}
