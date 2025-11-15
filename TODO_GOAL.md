now integrate this details to spring application. export interface Goal {
id?: number;
name: string;
category: 'short-term' | 'medium-term' | 'long-term' | 'retirement';
duration: number | null; // in years
targetAmount: number | null;
notes?: string;
inflationAdjustedAmount?: number | null;
} like you integrate expense management

I'll integrate the Goal management functionality into your Spring Boot application. Here's the complete implementation:

## 1. Goal Entity

### Goal.java
```java
@Entity
@Table(name = "goals")
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private GoalCategory category;
    
    @Column(name = "duration", nullable = false)
    private Integer duration; // in years
    
    @Column(name = "target_amount", nullable = false)
    private Double targetAmount;
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @Column(name = "inflation_adjusted_amount")
    private Double inflationAdjustedAmount;
    
    @Column(name = "inflation_rate")
    private Double inflationRate = 6.0; // Default inflation rate
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Enums
    public enum GoalCategory {
        SHORT_TERM, MEDIUM_TERM, LONG_TERM, RETIREMENT
    }
    
    // Constructors
    public Goal() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Goal(String name, GoalCategory category, Integer duration, Double targetAmount) {
        this();
        this.name = name;
        this.category = category;
        this.duration = duration;
        this.targetAmount = targetAmount;
        calculateInflationAdjustedAmount();
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
    
    public GoalCategory getCategory() {
        return category;
    }
    
    public void setCategory(GoalCategory category) {
        this.category = category;
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
        calculateInflationAdjustedAmount();
    }
    
    public Double getTargetAmount() {
        return targetAmount;
    }
    
    public void setTargetAmount(Double targetAmount) {
        this.targetAmount = targetAmount;
        calculateInflationAdjustedAmount();
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public Double getInflationAdjustedAmount() {
        return inflationAdjustedAmount;
    }
    
    public void setInflationAdjustedAmount(Double inflationAdjustedAmount) {
        this.inflationAdjustedAmount = inflationAdjustedAmount;
    }
    
    public Double getInflationRate() {
        return inflationRate;
    }
    
    public void setInflationRate(Double inflationRate) {
        this.inflationRate = inflationRate;
        calculateInflationAdjustedAmount();
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
    
    // Calculation Methods
    public void calculateInflationAdjustedAmount() {
        if (targetAmount != null && duration != null && inflationRate != null) {
            // Calculate inflation adjusted amount: FV = PV * (1 + r)^n
            double adjustedAmount = targetAmount * Math.pow(1 + (inflationRate / 100), duration);
            this.inflationAdjustedAmount = Math.round(adjustedAmount * 100.0) / 100.0;
        }
    }
    
    // Calculate monthly savings required (assuming 0% return for simplicity)
    public Double getMonthlySavingsRequired() {
        if (targetAmount != null && duration != null) {
            double totalMonths = duration * 12.0;
            double monthlySavings = targetAmount / totalMonths;
            return Math.round(monthlySavings * 100.0) / 100.0;
        }
        return 0.0;
    }
    
    // Calculate monthly savings with expected return
    public Double getMonthlySavingsWithReturn(Double expectedReturn) {
        if (targetAmount != null && duration != null && expectedReturn != null) {
            double monthlyRate = expectedReturn / 12 / 100;
            double months = duration * 12.0;
            
            // FV = PMT * [((1 + r)^n - 1) / r]
            // PMT = FV / [((1 + r)^n - 1) / r]
            double monthlySavings = targetAmount / (((Math.pow(1 + monthlyRate, months) - 1) / monthlyRate));
            return Math.round(monthlySavings * 100.0) / 100.0;
        }
        return 0.0;
    }
    
    // Calculate progress based on current savings
    public Map<String, Object> calculateProgress(Double currentSavings) {
        Map<String, Object> progress = new HashMap<>();
        
        if (targetAmount != null && currentSavings != null) {
            double percentage = (currentSavings / targetAmount) * 100;
            double remainingAmount = targetAmount - currentSavings;
            
            progress.put("currentSavings", currentSavings);
            progress.put("targetAmount", targetAmount);
            progress.put("percentage", Math.round(percentage * 100.0) / 100.0);
            progress.put("remainingAmount", Math.round(remainingAmount * 100.0) / 100.0);
            progress.put("isCompleted", percentage >= 100);
        }
        
        return progress;
    }
    
    // Get category color for UI
    public String getCategoryColor() {
        switch (category) {
            case SHORT_TERM: return "info";
            case MEDIUM_TERM: return "warning";
            case LONG_TERM: return "primary";
            case RETIREMENT: return "success";
            default: return "secondary";
        }
    }
    
    // Get category icon for UI
    public String getCategoryIcon() {
        switch (category) {
            case SHORT_TERM: return "fa-bolt";
            case MEDIUM_TERM: return "fa-chart-line";
            case LONG_TERM: return "fa-mountain";
            case RETIREMENT: return "fa-umbrella-beach";
            default: return "fa-bullseye";
        }
    }
    
    // Get priority based on category and duration
    public String getPriority() {
        switch (category) {
            case SHORT_TERM: return "HIGH";
            case MEDIUM_TERM: return "MEDIUM";
            case LONG_TERM: return "LOW";
            case RETIREMENT: return "VERY_HIGH";
            default: return "MEDIUM";
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateInflationAdjustedAmount();
    }
    
    @Override
    public String toString() {
        return "Goal{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", duration=" + duration +
                ", targetAmount=" + targetAmount +
                ", inflationAdjustedAmount=" + inflationAdjustedAmount +
                '}';
    }
}
```

## 2. Goal Repository

### GoalRepository.java
```java
@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByUser(User user);
    List<Goal> findByUserOrderByCreatedAtDesc(User user);
    List<Goal> findByUserOrderByCategoryAscCreatedAtDesc(User user);
    Optional<Goal> findByIdAndUser(Long id, User user);
    void deleteByIdAndUser(Long id, User user);
    
    List<Goal> findByUserAndCategory(User user, Goal.GoalCategory category);
    
    @Query("SELECT g FROM Goal g WHERE g.user = :user AND g.duration <= :maxDuration")
    List<Goal> findByUserAndDurationLessThanEqual(@Param("user") User user, @Param("maxDuration") Integer maxDuration);
    
    @Query("SELECT g FROM Goal g WHERE g.user = :user AND g.targetAmount <= :maxAmount")
    List<Goal> findByUserAndTargetAmountLessThanEqual(@Param("user") User user, @Param("maxAmount") Double maxAmount);
    
    @Query("SELECT COUNT(g) FROM Goal g WHERE g.user = :user AND g.category = :category")
    Long countByUserAndCategory(@Param("user") User user, @Param("category") Goal.GoalCategory category);
    
    @Query("SELECT SUM(g.targetAmount) FROM Goal g WHERE g.user = :user")
    Optional<Double> findTotalTargetAmountByUser(@Param("user") User user);
    
    @Query("SELECT g.category, COUNT(g) FROM Goal g WHERE g.user = :user GROUP BY g.category")
    List<Object[]> countByCategoryForUser(@Param("user") User user);
    
    @Query("SELECT g.category, SUM(g.targetAmount) FROM Goal g WHERE g.user = :user GROUP BY g.category")
    List<Object[]> sumTargetAmountByCategoryForUser(@Param("user") User user);
}
```

## 3. Goal Service

### GoalService.java
```java
@Service
@Transactional
public class GoalService {
    
    @Autowired
    private GoalRepository goalRepository;
    
    @Autowired
    private UserService userService;
    
    public Goal createGoal(Goal goal, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        goal.setUser(user);
        goal.calculateInflationAdjustedAmount(); // Calculate derived fields
        return goalRepository.save(goal);
    }
    
    @Transactional(readOnly = true)
    public List<Goal> getUserGoals(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        return goalRepository.findByUserOrderByCategoryAscCreatedAtDesc(user);
    }
    
    @Transactional(readOnly = true)
    public Goal getGoalByIdAndUser(Long id, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        return goalRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Goal not found with id: " + id));
    }
    
    public Goal updateGoal(Long id, Goal goalDetails, String username) {
        Goal goal = getGoalByIdAndUser(id, username);
        
        // Update all fields
        goal.setName(goalDetails.getName());
        goal.setCategory(goalDetails.getCategory());
        goal.setDuration(goalDetails.getDuration());
        goal.setTargetAmount(goalDetails.getTargetAmount());
        goal.setNotes(goalDetails.getNotes());
        goal.setInflationRate(goalDetails.getInflationRate());
        
        // Recalculate derived fields
        goal.calculateInflationAdjustedAmount();
        
        return goalRepository.save(goal);
    }
    
    public void deleteGoal(Long id, String username) {
        Goal goal = getGoalByIdAndUser(id, username);
        goalRepository.delete(goal);
    }
    
    @Transactional(readOnly = true)
    public List<Goal> getGoalsByCategory(String username, Goal.GoalCategory category) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        return goalRepository.findByUserAndCategory(user, category);
    }
    
    @Transactional(readOnly = true)
    public List<Goal> getShortTermGoals(String username) {
        return getGoalsByCategory(username, Goal.GoalCategory.SHORT_TERM);
    }
    
    @Transactional(readOnly = true)
    public List<Goal> getMediumTermGoals(String username) {
        return getGoalsByCategory(username, Goal.GoalCategory.MEDIUM_TERM);
    }
    
    @Transactional(readOnly = true)
    public List<Goal> getLongTermGoals(String username) {
        return getGoalsByCategory(username, Goal.GoalCategory.LONG_TERM);
    }
    
    @Transactional(readOnly = true)
    public List<Goal> getRetirementGoals(String username) {
        return getGoalsByCategory(username, Goal.GoalCategory.RETIREMENT);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getGoalsSummary(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        List<Goal> goals = goalRepository.findByUser(user);
        
        double totalTargetAmount = goalRepository.findTotalTargetAmountByUser(user).orElse(0.0);
        
        // Count by category
        Map<Goal.GoalCategory, Long> categoryCounts = new HashMap<>();
        List<Object[]> categoryCountsResult = goalRepository.countByCategoryForUser(user);
        for (Object[] result : categoryCountsResult) {
            Goal.GoalCategory category = (Goal.GoalCategory) result[0];
            Long count = (Long) result[1];
            categoryCounts.put(category, count);
        }
        
        // Sum by category
        Map<Goal.GoalCategory, Double> categoryAmounts = new HashMap<>();
        List<Object[]> categoryAmountsResult = goalRepository.sumTargetAmountByCategoryForUser(user);
        for (Object[] result : categoryAmountsResult) {
            Goal.GoalCategory category = (Goal.GoalCategory) result[0];
            Double amount = (Double) result[1];
            categoryAmounts.put(category, amount);
        }
        
        // Calculate average duration
        double averageDuration = goals.stream()
                .mapToInt(Goal::getDuration)
                .average()
                .orElse(0.0);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalGoals", goals.size());
        summary.put("totalTargetAmount", Math.round(totalTargetAmount * 100.0) / 100.0);
        summary.put("categoryDistribution", categoryCounts);
        summary.put("categoryAmounts", categoryAmounts);
        summary.put("averageDuration", Math.round(averageDuration * 100.0) / 100.0);
        summary.put("shortTermGoals", categoryCounts.getOrDefault(Goal.GoalCategory.SHORT_TERM, 0L));
        summary.put("mediumTermGoals", categoryCounts.getOrDefault(Goal.GoalCategory.MEDIUM_TERM, 0L));
        summary.put("longTermGoals", categoryCounts.getOrDefault(Goal.GoalCategory.LONG_TERM, 0L));
        summary.put("retirementGoals", categoryCounts.getOrDefault(Goal.GoalCategory.RETIREMENT, 0L));
        
        return summary;
    }
    
    public Map<String, Object> calculateGoalPlanning(Long id, Double expectedReturn, String username) {
        Goal goal = getGoalByIdAndUser(id, username);
        
        Map<String, Object> planning = new HashMap<>();
        planning.put("goal", goal);
        planning.put("monthlySavingsNoReturn", goal.getMonthlySavingsRequired());
        planning.put("monthlySavingsWithReturn", goal.getMonthlySavingsWithReturn(expectedReturn));
        planning.put("totalMonths", goal.getDuration() * 12);
        planning.put("totalYears", goal.getDuration());
        planning.put("inflationAdjustedAmount", goal.getInflationAdjustedAmount());
        
        // Calculate yearly breakdown
        List<Map<String, Object>> yearlyBreakdown = new ArrayList<>();
        double monthlySavings = goal.getMonthlySavingsWithReturn(expectedReturn);
        double monthlyRate = expectedReturn / 12 / 100;
        double balance = 0.0;
        
        for (int year = 1; year <= goal.getDuration(); year++) {
            for (int month = 1; month <= 12; month++) {
                balance = (balance + monthlySavings) * (1 + monthlyRate);
            }
            
            Map<String, Object> yearlyData = new HashMap<>();
            yearlyData.put("year", year);
            yearlyData.put("totalContributed", Math.round(monthlySavings * 12 * year * 100.0) / 100.0);
            yearlyData.put("balance", Math.round(balance * 100.0) / 100.0);
            yearlyData.put("interestEarned", Math.round((balance - (monthlySavings * 12 * year)) * 100.0) / 100.0);
            
            yearlyBreakdown.add(yearlyData);
        }
        
        planning.put("yearlyBreakdown", yearlyBreakdown);
        
        return planning;
    }
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUpcomingGoals(String username, int yearsAhead) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        List<Goal> goals = goalRepository.findByUserAndDurationLessThanEqual(user, yearsAhead);
        
        return goals.stream()
                .sorted(Comparator.comparing(Goal::getDuration))
                .map(goal -> {
                    Map<String, Object> upcomingGoal = new HashMap<>();
                    upcomingGoal.put("id", goal.getId());
                    upcomingGoal.put("name", goal.getName());
                    upcomingGoal.put("category", goal.getCategory());
                    upcomingGoal.put("duration", goal.getDuration());
                    upcomingGoal.put("targetAmount", goal.getTargetAmount());
                    upcomingGoal.put("inflationAdjustedAmount", goal.getInflationAdjustedAmount());
                    upcomingGoal.put("monthlySavingsRequired", goal.getMonthlySavingsRequired());
                    upcomingGoal.put("priority", goal.getPriority());
                    return upcomingGoal;
                })
                .collect(Collectors.toList());
    }
    
    public Map<String, Object> trackGoalProgress(Long id, Double currentSavings, String username) {
        Goal goal = getGoalByIdAndUser(id, username);
        return goal.calculateProgress(currentSavings);
    }
}
```

## 4. Goal Controller

### GoalController.java
```java
@RestController
@RequestMapping("/api/goals")
public class GoalController {
    
    @Autowired
    private GoalService goalService;
    
    @PostMapping
    public ResponseEntity<?> createGoal(@RequestBody Goal goal, Principal principal) {
        try {
            Goal savedGoal = goalService.createGoal(goal, principal.getName());
            return ResponseEntity.ok(savedGoal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating goal: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Goal>> getUserGoals(Principal principal) {
        try {
            List<Goal> goals = goalService.getUserGoals(principal.getName());
            return ResponseEntity.ok(goals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getGoalById(@PathVariable Long id, Principal principal) {
        try {
            Goal goal = goalService.getGoalByIdAndUser(id, principal.getName());
            return ResponseEntity.ok(goal);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGoal(@PathVariable Long id, @RequestBody Goal goal, Principal principal) {
        try {
            Goal updatedGoal = goalService.updateGoal(id, goal, principal.getName());
            return ResponseEntity.ok(updatedGoal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating goal: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@PathVariable Long id, Principal principal) {
        try {
            goalService.deleteGoal(id, principal.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting goal: " + e.getMessage());
        }
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getGoalsByCategory(@PathVariable String category, Principal principal) {
        try {
            Goal.GoalCategory goalCategory = Goal.GoalCategory.valueOf(category.toUpperCase());
            List<Goal> goals = goalService.getGoalsByCategory(principal.getName(), goalCategory);
            return ResponseEntity.ok(goals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid category or error retrieving goals: " + e.getMessage());
        }
    }
    
    @GetMapping("/short-term")
    public ResponseEntity<List<Goal>> getShortTermGoals(Principal principal) {
        try {
            List<Goal> goals = goalService.getShortTermGoals(principal.getName());
            return ResponseEntity.ok(goals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }
    
    @GetMapping("/medium-term")
    public ResponseEntity<List<Goal>> getMediumTermGoals(Principal principal) {
        try {
            List<Goal> goals = goalService.getMediumTermGoals(principal.getName());
            return ResponseEntity.ok(goals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }
    
    @GetMapping("/long-term")
    public ResponseEntity<List<Goal>> getLongTermGoals(Principal principal) {
        try {
            List<Goal> goals = goalService.getLongTermGoals(principal.getName());
            return ResponseEntity.ok(goals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }
    
    @GetMapping("/retirement")
    public ResponseEntity<List<Goal>> getRetirementGoals(Principal principal) {
        try {
            List<Goal> goals = goalService.getRetirementGoals(principal.getName());
            return ResponseEntity.ok(goals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }
    
    @GetMapping("/summary/overview")
    public ResponseEntity<Map<String, Object>> getGoalsSummary(Principal principal) {
        try {
            Map<String, Object> summary = goalService.getGoalsSummary(principal.getName());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyMap());
        }
    }
    
    @GetMapping("/{id}/planning")
    public ResponseEntity<?> calculateGoalPlanning(
            @PathVariable Long id,
            @RequestParam Double expectedReturn,
            Principal principal) {
        try {
            Map<String, Object> planning = goalService.calculateGoalPlanning(id, expectedReturn, principal.getName());
            return ResponseEntity.ok(planning);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error calculating goal planning: " + e.getMessage());
        }
    }
    
    @GetMapping("/upcoming")
    public ResponseEntity<?> getUpcomingGoals(
            @RequestParam(defaultValue = "5") int yearsAhead,
            Principal principal) {
        try {
            List<Map<String, Object>> upcomingGoals = goalService.getUpcomingGoals(principal.getName(), yearsAhead);
            return ResponseEntity.ok(upcomingGoals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving upcoming goals: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/track-progress")
    public ResponseEntity<?> trackGoalProgress(
            @PathVariable Long id,
            @RequestParam Double currentSavings,
            Principal principal) {
        try {
            Map<String, Object> progress = goalService.trackGoalProgress(id, currentSavings, principal.getName());
            return ResponseEntity.ok(progress);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error tracking goal progress: " + e.getMessage());
        }
    }
}
```

## 5. Angular Goal Model

### goal.model.ts
```typescript
export interface Goal {
  id?: number;
  name: string;
  category: 'SHORT_TERM' | 'MEDIUM_TERM' | 'LONG_TERM' | 'RETIREMENT';
  duration: number | null;
  targetAmount: number | null;
  notes?: string;
  inflationAdjustedAmount?: number | null;
  inflationRate?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface GoalSummary {
  totalGoals: number;
  totalTargetAmount: number;
  categoryDistribution: { [key: string]: number };
  categoryAmounts: { [key: string]: number };
  averageDuration: number;
  shortTermGoals: number;
  mediumTermGoals: number;
  longTermGoals: number;
  retirementGoals: number;
}

export interface GoalPlanning {
  goal: Goal;
  monthlySavingsNoReturn: number;
  monthlySavingsWithReturn: number;
  totalMonths: number;
  totalYears: number;
  inflationAdjustedAmount: number;
  yearlyBreakdown: YearlyBreakdown[];
}

export interface YearlyBreakdown {
  year: number;
  totalContributed: number;
  balance: number;
  interestEarned: number;
}

export interface UpcomingGoal {
  id: number;
  name: string;
  category: string;
  duration: number;
  targetAmount: number;
  inflationAdjustedAmount: number;
  monthlySavingsRequired: number;
  priority: string;
}

export interface GoalProgress {
  currentSavings: number;
  targetAmount: number;
  percentage: number;
  remainingAmount: number;
  isCompleted: boolean;
}
```

## 6. Angular Goal Service

### goal.service.ts
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { 
  Goal, 
  GoalSummary, 
  GoalPlanning, 
  UpcomingGoal,
  GoalProgress 
} from '../models/goal.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class GoalService {
  private apiUrl = 'http://localhost:8080/api/goals';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  private getAuthHeaders(): HttpHeaders {
    return this.authService.getAuthHeaders();
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unknown error occurred!';
    if (error.error instanceof ErrorEvent) {
      errorMessage = `Error: ${error.error.message}`;
    } else {
      if (error.status === 401) {
        errorMessage = 'Unauthorized: Please login again.';
        this.authService.logout();
      } else if (error.status === 403) {
        errorMessage = 'Forbidden: You do not have permission to perform this action.';
      } else if (error.status === 404) {
        errorMessage = 'Not Found: The requested resource was not found.';
      } else {
        errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
      }
    }
    console.error(errorMessage);
    return throwError(errorMessage);
  }

  createGoal(goal: Goal): Observable<Goal> {
    return this.http.post<Goal>(this.apiUrl, goal, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getUserGoals(): Observable<Goal[]> {
    return this.http.get<Goal[]>(this.apiUrl, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getGoalById(id: number): Observable<Goal> {
    return this.http.get<Goal>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  updateGoal(id: number, goal: Goal): Observable<Goal> {
    return this.http.put<Goal>(`${this.apiUrl}/${id}`, goal, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  deleteGoal(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getGoalsByCategory(category: string): Observable<Goal[]> {
    return this.http.get<Goal[]>(`${this.apiUrl}/category/${category}`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getShortTermGoals(): Observable<Goal[]> {
    return this.http.get<Goal[]>(`${this.apiUrl}/short-term`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getMediumTermGoals(): Observable<Goal[]> {
    return this.http.get<Goal[]>(`${this.apiUrl}/medium-term`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getLongTermGoals(): Observable<Goal[]> {
    return this.http.get<Goal[]>(`${this.apiUrl}/long-term`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getRetirementGoals(): Observable<Goal[]> {
    return this.http.get<Goal[]>(`${this.apiUrl}/retirement`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getGoalsSummary(): Observable<GoalSummary> {
    return this.http.get<GoalSummary>(`${this.apiUrl}/summary/overview`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  calculateGoalPlanning(id: number, expectedReturn: number): Observable<GoalPlanning> {
    const params = new HttpParams().set('expectedReturn', expectedReturn.toString());
    return this.http.get<GoalPlanning>(`${this.apiUrl}/${id}/planning`, { 
      headers: this.getAuthHeaders(),
      params: params
    }).pipe(catchError(this.handleError));
  }

  getUpcomingGoals(yearsAhead: number = 5): Observable<UpcomingGoal[]> {
    const params = new HttpParams().set('yearsAhead', yearsAhead.toString());
    return this.http.get<UpcomingGoal[]>(`${this.apiUrl}/upcoming`, { 
      headers: this.getAuthHeaders(),
      params: params
    }).pipe(catchError(this.handleError));
  }

  trackGoalProgress(id: number, currentSavings: number): Observable<GoalProgress> {
    const params = new HttpParams().set('currentSavings', currentSavings.toString());
    return this.http.post<GoalProgress>(`${this.apiUrl}/${id}/track-progress`, {}, { 
      headers: this.getAuthHeaders(),
      params: params
    }).pipe(catchError(this.handleError));
  }
}
```

## 7. Goal List Component

### goal-list.component.ts
```typescript
import { Component, OnInit } from '@angular/core';
import { Goal, GoalSummary, UpcomingGoal } from '../../models/goal.model';
import { GoalService } from '../../services/goal.service';

@Component({
  selector: 'app-goal-list',
  templateUrl: './goal-list.component.html',
  styleUrls: ['./goal-list.component.css']
})
export class GoalListComponent implements OnInit {
  goals: Goal[] = [];
  summary: GoalSummary | null = null;
  upcomingGoals: UpcomingGoal[] = [];
  isLoading: boolean = false;
  errorMessage: string = '';
  
  // Filter properties
  selectedCategory: string = '';
  maxDuration: number = 50;
  maxAmount: number = 1000000;

  // Categories for dropdown
  categories = ['SHORT_TERM', 'MEDIUM_TERM', 'LONG_TERM', 'RETIREMENT'];

  constructor(private goalService: GoalService) { }

  ngOnInit(): void {
    this.loadGoals();
    this.loadSummary();
    this.loadUpcomingGoals();
  }

  loadGoals(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.goalService.getUserGoals().subscribe({
      next: (goals) => {
        this.goals = goals;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load goals. Please try again.';
        this.isLoading = false;
        console.error('Error loading goals:', error);
      }
    });
  }

  loadSummary(): void {
    this.goalService.getGoalsSummary().subscribe({
      next: (summary) => {
        this.summary = summary;
      },
      error: (error) => {
        console.error('Error loading goals summary:', error);
      }
    });
  }

  loadUpcomingGoals(): void {
    this.goalService.getUpcomingGoals(5).subscribe({
      next: (upcomingGoals) => {
        this.upcomingGoals = upcomingGoals;
      },
      error: (error) => {
        console.error('Error loading upcoming goals:', error);
      }
    });
  }

  deleteGoal(id: number): void {
    if (confirm('Are you sure you want to delete this goal?')) {
      this.goalService.deleteGoal(id).subscribe({
        next: () => {
          this.goals = this.goals.filter(goal => goal.id !== id);
          this.loadSummary();
          this.loadUpcomingGoals();
        },
        error: (error) => {
          this.errorMessage = 'Failed to delete goal. Please try again.';
          console.error('Error deleting goal:', error);
        }
      });
    }
  }

  applyFilters(): void {
    if (this.selectedCategory) {
      this.goalService.getGoalsByCategory(this.selectedCategory).subscribe({
        next: (goals) => {
          this.goals = goals.filter(goal => 
            (goal.duration || 0) <= this.maxDuration && 
            (goal.targetAmount || 0) <= this.maxAmount
          );
        },
        error: (error) => {
          this.errorMessage = 'Error applying filters.';
        }
      });
    } else {
      this.goals = this.goals.filter(goal => 
        (goal.duration || 0) <= this.maxDuration && 
        (goal.targetAmount || 0) <= this.maxAmount
      );
    }
  }

  clearFilters(): void {
    this.selectedCategory = '';
    this.maxDuration = 50;
    this.maxAmount = 1000000;
    this.loadGoals();
  }

  getCategoryColor(category: string): string {
    switch (category) {
      case 'SHORT_TERM': return 'info';
      case 'MEDIUM_TERM': return 'warning';
      case 'LONG_TERM': return 'primary';
      case 'RETIREMENT': return 'success';
      default: return 'secondary';
    }
  }

  getCategoryIcon(category: string): string {
    switch (category) {
      case 'SHORT_TERM': return 'fa-bolt';
      case 'MEDIUM_TERM': return 'fa-chart-line';
      case 'LONG_TERM': return 'fa-mountain';
      case 'RETIREMENT': return 'fa-umbrella-beach';
      default: return 'fa-bullseye';
    }
  }

  getCategoryDisplayName(category: string): string {
    switch (category) {
      case 'SHORT_TERM': return 'Short Term';
      case 'MEDIUM_TERM': return 'Medium Term';
      case 'LONG_TERM': return 'Long Term';
      case 'RETIREMENT': return 'Retirement';
      default: return category;
    }
  }

  calculateMonthlySavings(goal: Goal): number {
    if (!goal.targetAmount || !goal.duration) return 0;
    return goal.targetAmount / (goal.duration * 12);
  }

  getCategoryStats(): any[] {
    if (!this.summary?.categoryDistribution) return [];
    
    const total = this.summary.totalGoals;
    return Object.entries(this.summary.categoryDistribution).map(([category, count]) => ({
      category,
      count,
      percentage: (count / total) * 100,
      amount: this.summary?.categoryAmounts[category] || 0
    }));
  }
}
```

## 8. Goal Form Component

### goal-form.component.ts
```typescript
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { GoalService } from '../../services/goal.service';
import { Goal } from '../../models/goal.model';

@Component({
  selector: 'app-goal-form',
  templateUrl: './goal-form.component.html',
  styleUrls: ['./goal-form.component.css']
})
export class GoalFormComponent implements OnInit {
  goalForm: FormGroup;
  isEdit = false;
  goalId?: number;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  calculatedValues: any = {};

  // Dropdown options
  categories = ['SHORT_TERM', 'MEDIUM_TERM', 'LONG_TERM', 'RETIREMENT'];

  constructor(
    private fb: FormBuilder,
    private goalService: GoalService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.goalForm = this.createForm();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEdit = true;
        this.goalId = +params['id'];
        this.loadGoal(this.goalId);
      }
    });

    // Recalculate when form values change
    this.goalForm.valueChanges.subscribe(() => {
      this.calculateInflationAdjustedAmount();
    });
  }

  createForm(): FormGroup {
    return this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      category: ['', Validators.required],
      duration: [null, [Validators.required, Validators.min(1), Validators.max(50)]],
      targetAmount: [null, [Validators.required, Validators.min(1), Validators.max(10000000)]],
      inflationRate: [6, [Validators.min(0), Validators.max(20)]],
      notes: ['']
    });
  }

  loadGoal(id: number): void {
    this.isLoading = true;
    this.goalService.getGoalById(id).subscribe({
      next: (goal) => {
        this.goalForm.patchValue({
          name: goal.name,
          category: goal.category,
          duration: goal.duration,
          targetAmount: goal.targetAmount,
          inflationRate: goal.inflationRate || 6,
          notes: goal.notes || ''
        });
        this.calculatedValues = {
          inflationAdjustedAmount: goal.inflationAdjustedAmount
        };
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load goal. Please try again.';
        this.isLoading = false;
        console.error('Error loading goal:', error);
      }
    });
  }

  calculateInflationAdjustedAmount(): void {
    const formValue = this.goalForm.value;
    if (formValue.targetAmount && formValue.duration && formValue.inflationRate) {
      const targetAmount = formValue.targetAmount;
      const duration = formValue.duration;
      const inflationRate = formValue.inflationRate / 100;

      // Calculate inflation adjusted amount: FV = PV * (1 + r)^n
      const inflationAdjustedAmount = targetAmount * Math.pow(1 + inflationRate, duration);
      const monthlySavings = targetAmount / (duration * 12);

      this.calculatedValues = {
        inflationAdjustedAmount: Math.round(inflationAdjustedAmount * 100) / 100,
        monthlySavings: Math.round(monthlySavings * 100) / 100,
        totalMonths: duration * 12
      };
    } else {
      this.calculatedValues = {};
    }
  }

  onSubmit(): void {
    if (this.goalForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const goal: Goal = this.goalForm.value;

      const operation = this.isEdit && this.goalId
        ? this.goalService.updateGoal(this.goalId, goal)
        : this.goalService.createGoal(goal);

      operation.subscribe({
        next: (savedGoal) => {
          this.isLoading = false;
          this.successMessage = this.isEdit 
            ? 'Goal updated successfully!' 
            : 'Goal created successfully!';
          
          setTimeout(() => {
            this.router.navigate(['/goals']);
          }, 1500);
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = this.isEdit
            ? 'Failed to update goal. Please try again.'
            : 'Failed to create goal. Please try again.';
          console.error('Error saving goal:', error);
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  markFormGroupTouched(): void {
    Object.keys(this.goalForm.controls).forEach(key => {
      const control = this.goalForm.get(key);
      control?.markAsTouched();
    });
  }

  onCancel(): void {
    if (this.goalForm.dirty) {
      if (confirm('You have unsaved changes. Are you sure you want to leave?')) {
        this.router.navigate(['/goals']);
      }
    } else {
      this.router.navigate(['/goals']);
    }
  }

  // Getters for easy access in template
  get name() { return this.goalForm.get('name'); }
  get category() { return this.goalForm.get('category'); }
  get duration() { return this.goalForm.get('duration'); }
  get targetAmount() { return this.goalForm.get('targetAmount'); }
  get inflationRate() { return this.goalForm.get('inflationRate'); }
  get notes() { return this.goalForm.get('notes'); }
}
```

## 9. Update App Routing

### app-routing.module.ts
```typescript
const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'expenses', component: ExpenseListComponent, canActivate: [AuthGuard] },
  { path: 'expenses/new', component: ExpenseFormComponent, canActivate: [AuthGuard] },
  { path: 'expenses/edit/:id', component: ExpenseFormComponent, canActivate: [AuthGuard] },
  { path: 'emis', component: EMIListComponent, canActivate: [AuthGuard] },
  { path: 'emis/new', component: EMIFormComponent, canActivate: [AuthGuard] },
  { path: 'emis/edit/:id', component: EMIFormComponent, canActivate: [AuthGuard] },
  { path: 'sips', component: SIPListComponent, canActivate: [AuthGuard] },
  { path: 'sips/new', component: SIPFormComponent, canActivate: [AuthGuard] },
  { path: 'sips/edit/:id', component: SIPFormComponent, canActivate: [AuthGuard] },
  { path: 'lump-sums', component: LumpSumListComponent, canActivate: [AuthGuard] },
  { path: 'lump-sums/new', component: LumpSumFormComponent, canActivate: [AuthGuard] },
  { path: 'lump-sums/edit/:id', component: LumpSumFormComponent, canActivate: [AuthGuard] },
  { path: 'income-sources', component: IncomeSourceListComponent, canActivate: [AuthGuard] },
  { path: 'income-sources/new', component: IncomeSourceFormComponent, canActivate: [AuthGuard] },
  { path: 'income-sources/edit/:id', component: IncomeSourceFormComponent, canActivate: [AuthGuard] },
  { path: 'investment-options', component: InvestmentOptionListComponent, canActivate: [AuthGuard] },
  { path: 'investment-options/new', component: InvestmentOptionFormComponent, canActivate: [AuthGuard] },
  { path: 'investment-options/edit/:id', component: InvestmentOptionFormComponent, canActivate: [AuthGuard] },
  { path: 'goals', component: GoalListComponent, canActivate: [AuthGuard] },
  { path: 'goals/new', component: GoalFormComponent, canActivate: [AuthGuard] },
  { path: 'goals/edit/:id', component: GoalFormComponent, canActivate: [AuthGuard] },
  { path: '', redirectTo: '/expenses', pathMatch: 'full' },
  { path: '**', redirectTo: '/expenses' }
];
```

## 10. Update App Module

### app.module.ts
```typescript
@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    ExpenseListComponent,
    ExpenseFormComponent,
    EMIListComponent,
    EMIFormComponent,
    SIPListComponent,
    SIPFormComponent,
    LumpSumListComponent,
    LumpSumFormComponent,
    IncomeSourceListComponent,
    IncomeSourceFormComponent,
    InvestmentOptionListComponent,
    InvestmentOptionFormComponent,
    GoalListComponent,
    GoalFormComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
```

## 11. CURL Commands for Testing

```bash
# Get JWT token first
export JWT_TOKEN="your-jwt-token"

# Create Goal
curl -X POST http://localhost:8080/api/goals \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "Down Payment for House",
    "category": "MEDIUM_TERM",
    "duration": 5,
    "targetAmount": 500000,
    "inflationRate": 6,
    "notes": "Save for down payment on a new house"
  }'

# Get all goals
curl -X GET http://localhost:8080/api/goals \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get goals by category
curl -X GET http://localhost:8080/api/goals/category/MEDIUM_TERM \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get goals summary
curl -X GET http://localhost:8080/api/goals/summary/overview \
  -H "Authorization: Bearer $JWT_TOKEN"

# Calculate goal planning
curl -X GET "http://localhost:8080/api/goals/1/planning?expectedReturn=12" \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get upcoming goals
curl -X GET "http://localhost:8080/api/goals/upcoming?yearsAhead=3" \
  -H "Authorization: Bearer $JWT_TOKEN"

# Track goal progress
curl -X POST "http://localhost:8080/api/goals/1/track-progress?currentSavings=100000" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## Key Features:

1. **Goal Categorization**: Short-term, medium-term, long-term, and retirement goals
2. **Inflation Adjustment**: Automatic calculation of inflation-adjusted target amounts
3. **Savings Planning**: Calculate required monthly savings with and without returns
4. **Progress Tracking**: Track progress towards goals with current savings
5. **Goal Prioritization**: Automatic priority assignment based on category and duration
6. **Upcoming Goals**: Identify goals that are due soon
7. **Comprehensive Analytics**: Category distribution, total amounts, and averages
8. **Yearly Breakdown**: Detailed yearly savings and growth projections
9. **Flexible Inflation Rates**: Customizable inflation rates for accurate planning

The Goal management is now fully integrated with your Spring Boot backend and Angular frontend, completing your comprehensive financial management system with Expenses, EMIs, SIPs, LumpSum investments, Income Sources, Investment Options, and Financial Goals!