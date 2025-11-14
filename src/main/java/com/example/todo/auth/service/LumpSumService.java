package com.example.todo.auth.service;

import com.example.todo.auth.entity.LumpSum;
import com.example.todo.auth.entity.User;
import com.example.todo.auth.repository.LumpSumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class LumpSumService {

    @Autowired
    private LumpSumRepository lumpSumRepository;

    @Autowired
    private UserService userService;

    public LumpSum createLumpSum(LumpSum lumpSum, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        lumpSum.setUser(user);
        lumpSum.calculateLumpSum(); // Calculate all derived fields
        return lumpSumRepository.save(lumpSum);
    }

    @Transactional(readOnly = true)
    public List<LumpSum> getUserLumpSums(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return lumpSumRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public LumpSum getLumpSumByIdAndUser(Long id, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return lumpSumRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("LumpSum not found with id: " + id));
    }

    public LumpSum updateLumpSum(Long id, LumpSum lumpSumDetails, String username) {
        LumpSum lumpSum = getLumpSumByIdAndUser(id, username);

        // Update basic fields
        lumpSum.setInvestmentName(lumpSumDetails.getInvestmentName());
        lumpSum.setPrincipalAmount(lumpSumDetails.getPrincipalAmount());
        lumpSum.setDuration(lumpSumDetails.getDuration());
        lumpSum.setExpectedReturn(lumpSumDetails.getExpectedReturn());

        // Recalculate all derived fields
        lumpSum.calculateLumpSum();

        return lumpSumRepository.save(lumpSum);
    }

    public void deleteLumpSum(Long id, String username) {
        LumpSum lumpSum = getLumpSumByIdAndUser(id, username);
        lumpSumRepository.delete(lumpSum);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLumpSumSummary(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<LumpSum> lumpSums = lumpSumRepository.findByUser(user);

        double totalPrincipal = lumpSumRepository.findTotalPrincipalByUser(user).orElse(0.0);
        double totalFutureValue = lumpSumRepository.findTotalFutureValueByUser(user).orElse(0.0);
        double totalInterest = lumpSumRepository.findTotalInterestByUser(user).orElse(0.0);
        double averageReturn = lumpSumRepository.findAverageReturnByUser(user).orElse(0.0);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalLumpSums", lumpSums.size());
        summary.put("totalPrincipal", Math.round(totalPrincipal * 100.0) / 100.0);
        summary.put("totalFutureValue", Math.round(totalFutureValue * 100.0) / 100.0);
        summary.put("totalInterest", Math.round(totalInterest * 100.0) / 100.0);
        summary.put("averageReturn", Math.round(averageReturn * 100.0) / 100.0);
        summary.put("totalReturnPercentage", totalPrincipal > 0 ? Math.round((totalInterest / totalPrincipal) * 100 * 100.0) / 100.0 : 0.0);

        return summary;
    }

    public Map<String, Object> calculateLumpSumProgress(Long id, Integer yearsCompleted, String username) {
        LumpSum lumpSum = getLumpSumByIdAndUser(id, username);
//        return lumpSum.calculateForPeriod(yearsCompleted);
        return null;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLumpSumProjection(Long id, String username) {
        LumpSum lumpSum = getLumpSumByIdAndUser(id, username);
        return lumpSum.getYearlyProjection();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopPerformingInvestments(String username, int limit) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<LumpSum> lumpSums = lumpSumRepository.findByUser(user);

        return lumpSums.stream()
                .sorted((ls1, ls2) -> Double.compare(
                        (ls2.getFutureValue() - ls2.getPrincipalAmount()) / ls2.getPrincipalAmount(),
                        (ls1.getFutureValue() - ls1.getPrincipalAmount()) / ls1.getPrincipalAmount()
                ))
                .limit(limit)
                .map(ls -> {
                    Map<String, Object> performance = new HashMap<>();
                    performance.put("id", ls.getId());
                    performance.put("investmentName", ls.getInvestmentName());
                    performance.put("principalAmount", ls.getPrincipalAmount());
                    performance.put("futureValue", ls.getFutureValue());
                    performance.put("totalReturn", ls.getTotalInterest());
                    performance.put("returnPercentage", Math.round((ls.getTotalInterest() / ls.getPrincipalAmount()) * 100 * 100.0) / 100.0);
                    performance.put("duration", ls.getDuration());
                    return performance;
                })
                .collect(Collectors.toList());
    }
}
