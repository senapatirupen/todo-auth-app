now integrate this details to spring application. export interface IncomeSource {
id?: number;
sourceName: string;
initialMonthlyIncome: number | null;
annualGrowthRate: number | null;
years: number | null;
projectedMonthlyIncome: number | null;
totalAmountReceived: number | null;
} like you integrate expense management

I'll integrate the Income Source management functionality into your Spring Boot application. Here's the complete implementation:

## 1. IncomeSource Entity

### IncomeSource.java
```java
@Entity
@Table(name = "income_sources")
public class IncomeSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "source_name", nullable = false)
    private String sourceName;
    
    @Column(name = "initial_monthly_income", nullable = false)
    private Double initialMonthlyIncome;
    
    @Column(name = "annual_growth_rate", nullable = false)
    private Double annualGrowthRate; // percentage
    
    @Column(name = "years", nullable = false)
    private Integer years;
    
    @Column(name = "projected_monthly_income")
    private Double projectedMonthlyIncome;
    
    @Column(name = "total_amount_received")
    private Double totalAmountReceived;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public IncomeSource() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public IncomeSource(String sourceName, Double initialMonthlyIncome, Double annualGrowthRate, Integer years) {
        this();
        this.sourceName = sourceName;
        this.initialMonthlyIncome = initialMonthlyIncome;
        this.annualGrowthRate = annualGrowthRate;
        this.years = years;
        calculateIncomeProjection();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getSourceName() {
        return sourceName;
    }
    
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
    
    public Double getInitialMonthlyIncome() {
        return initialMonthlyIncome;
    }
    
    public void setInitialMonthlyIncome(Double initialMonthlyIncome) {
        this.initialMonthlyIncome = initialMonthlyIncome;
        calculateIncomeProjection();
    }
    
    public Double getAnnualGrowthRate() {
        return annualGrowthRate;
    }
    
    public void setAnnualGrowthRate(Double annualGrowthRate) {
        this.annualGrowthRate = annualGrowthRate;
        calculateIncomeProjection();
    }
    
    public Integer getYears() {
        return years;
    }
    
    public void setYears(Integer years) {
        this.years = years;
        calculateIncomeProjection();
    }
    
    public Double getProjectedMonthlyIncome() {
        return projectedMonthlyIncome;
    }
    
    public void setProjectedMonthlyIncome(Double projectedMonthlyIncome) {
        this.projectedMonthlyIncome = projectedMonthlyIncome;
    }
    
    public Double getTotalAmountReceived() {
        return totalAmountReceived;
    }
    
    public void setTotalAmountReceived(Double totalAmountReceived) {
        this.totalAmountReceived = totalAmountReceived;
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
    
    // Income Projection Calculation Methods
    public void calculateIncomeProjection() {
        if (initialMonthlyIncome != null && annualGrowthRate != null && years != null) {
            // Calculate projected monthly income after n years with annual growth
            double projectedIncome = initialMonthlyIncome * Math.pow(1 + (annualGrowthRate / 100), years);
            this.projectedMonthlyIncome = Math.round(projectedIncome * 100.0) / 100.0;
            
            // Calculate total amount received over the years
            double totalAmount = 0.0;
            double currentYearIncome = initialMonthlyIncome * 12; // Annual income for first year
            
            for (int year = 1; year <= years; year++) {
                totalAmount += currentYearIncome;
                currentYearIncome *= (1 + (annualGrowthRate / 100)); // Grow income for next year
            }
            
            this.totalAmountReceived = Math.round(totalAmount * 100.0) / 100.0;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    // Calculate year-by-year income progression
    public List<Map<String, Object>> getYearlyIncomeProgression() {
        List<Map<String, Object>> progression = new ArrayList<>();
        
        double currentMonthlyIncome = initialMonthlyIncome;
        
        for (int year = 1; year <= years; year++) {
            double annualIncome = currentMonthlyIncome * 12;
            double cumulativeIncome = 0.0;
            
            // Calculate cumulative income up to this year
            double tempMonthlyIncome = initialMonthlyIncome;
            for (int y = 1; y <= year; y++) {
                cumulativeIncome += tempMonthlyIncome * 12;
                tempMonthlyIncome *= (1 + (annualGrowthRate / 100));
            }
            
            Map<String, Object> yearlyData = new HashMap<>();
            yearlyData.put("year", year);
            yearlyData.put("monthlyIncome", Math.round(currentMonthlyIncome * 100.0) / 100.0);
            yearlyData.put("annualIncome", Math.round(annualIncome * 100.0) / 100.0);
            yearlyData.put("cumulativeIncome", Math.round(cumulativeIncome * 100.0) / 100.0);
            yearlyData.put("growthFromStart", Math.round(((currentMonthlyIncome - initialMonthlyIncome) / initialMonthlyIncome) * 100 * 100.0) / 100.0);
            
            progression.add(yearlyData);
            
            // Grow income for next year
            currentMonthlyIncome *= (1 + (annualGrowthRate / 100));
        }
        
        return progression;
    }
    
    // Calculate income for a specific year
    public Map<String, Object> calculateIncomeForYear(int targetYear) {
        if (targetYear < 1 || targetYear > years) {
            throw new IllegalArgumentException("Target year must be between 1 and " + years);
        }
        
        double monthlyIncome = initialMonthlyIncome * Math.pow(1 + (annualGrowthRate / 100), targetYear - 1);
        double annualIncome = monthlyIncome * 12;
        
        Map<String, Object> result = new HashMap<>();
        result.put("year", targetYear);
        result.put("monthlyIncome", Math.round(monthlyIncome * 100.0) / 100.0);
        result.put("annualIncome", Math.round(annualIncome * 100.0) / 100.0);
        result.put("growthFromPrevious", targetYear > 1 ? 
            Math.round((annualGrowthRate * 100.0) / 100.0) : 0.0);
        
        return result;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "IncomeSource{" +
                "id=" + id +
                ", sourceName='" + sourceName + '\'' +
                ", initialMonthlyIncome=" + initialMonthlyIncome +
                ", annualGrowthRate=" + annualGrowthRate +
                ", years=" + years +
                ", projectedMonthlyIncome=" + projectedMonthlyIncome +
                ", totalAmountReceived=" + totalAmountReceived +
                '}';
    }
}
```

## 2. IncomeSource Repository

### IncomeSourceRepository.java
```java
@Repository
public interface IncomeSourceRepository extends JpaRepository<IncomeSource, Long> {
    List<IncomeSource> findByUser(User user);
    List<IncomeSource> findByUserOrderByCreatedAtDesc(User user);
    Optional<IncomeSource> findByIdAndUser(Long id, User user);
    void deleteByIdAndUser(Long id, User user);
    
    @Query("SELECT SUM(is.initialMonthlyIncome) FROM IncomeSource is WHERE is.user = :user")
    Optional<Double> findTotalInitialMonthlyIncomeByUser(@Param("user") User user);
    
    @Query("SELECT SUM(is.projectedMonthlyIncome) FROM IncomeSource is WHERE is.user = :user")
    Optional<Double> findTotalProjectedMonthlyIncomeByUser(@Param("user") User user);
    
    @Query("SELECT SUM(is.totalAmountReceived) FROM IncomeSource is WHERE is.user = :user")
    Optional<Double> findTotalAmountReceivedByUser(@Param("user") User user);
    
    @Query("SELECT AVG(is.annualGrowthRate) FROM IncomeSource is WHERE is.user = :user")
    Optional<Double> findAverageGrowthRateByUser(@Param("user") User user);
}
```

## 3. IncomeSource Service

### IncomeSourceService.java
```java
@Service
@Transactional
public class IncomeSourceService {
    
    @Autowired
    private IncomeSourceRepository incomeSourceRepository;
    
    @Autowired
    private UserService userService;
    
    public IncomeSource createIncomeSource(IncomeSource incomeSource, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        incomeSource.setUser(user);
        incomeSource.calculateIncomeProjection(); // Calculate all derived fields
        return incomeSourceRepository.save(incomeSource);
    }
    
    @Transactional(readOnly = true)
    public List<IncomeSource> getUserIncomeSources(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        return incomeSourceRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    @Transactional(readOnly = true)
    public IncomeSource getIncomeSourceByIdAndUser(Long id, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        return incomeSourceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Income source not found with id: " + id));
    }
    
    public IncomeSource updateIncomeSource(Long id, IncomeSource incomeSourceDetails, String username) {
        IncomeSource incomeSource = getIncomeSourceByIdAndUser(id, username);
        
        // Update basic fields
        incomeSource.setSourceName(incomeSourceDetails.getSourceName());
        incomeSource.setInitialMonthlyIncome(incomeSourceDetails.getInitialMonthlyIncome());
        incomeSource.setAnnualGrowthRate(incomeSourceDetails.getAnnualGrowthRate());
        incomeSource.setYears(incomeSourceDetails.getYears());
        
        // Recalculate all derived fields
        incomeSource.calculateIncomeProjection();
        
        return incomeSourceRepository.save(incomeSource);
    }
    
    public void deleteIncomeSource(Long id, String username) {
        IncomeSource incomeSource = getIncomeSourceByIdAndUser(id, username);
        incomeSourceRepository.delete(incomeSource);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getIncomeSourceSummary(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        List<IncomeSource> incomeSources = incomeSourceRepository.findByUser(user);
        
        double totalInitialMonthlyIncome = incomeSourceRepository.findTotalInitialMonthlyIncomeByUser(user).orElse(0.0);
        double totalProjectedMonthlyIncome = incomeSourceRepository.findTotalProjectedMonthlyIncomeByUser(user).orElse(0.0);
        double totalAmountReceived = incomeSourceRepository.findTotalAmountReceivedByUser(user).orElse(0.0);
        double averageGrowthRate = incomeSourceRepository.findAverageGrowthRateByUser(user).orElse(0.0);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncomeSources", incomeSources.size());
        summary.put("totalInitialMonthlyIncome", Math.round(totalInitialMonthlyIncome * 100.0) / 100.0);
        summary.put("totalProjectedMonthlyIncome", Math.round(totalProjectedMonthlyIncome * 100.0) / 100.0);
        summary.put("totalAmountReceived", Math.round(totalAmountReceived * 100.0) / 100.0);
        summary.put("averageGrowthRate", Math.round(averageGrowthRate * 100.0) / 100.0);
        summary.put("totalGrowthPercentage", totalInitialMonthlyIncome > 0 ? 
            Math.round(((totalProjectedMonthlyIncome - totalInitialMonthlyIncome) / totalInitialMonthlyIncome) * 100 * 100.0) / 100.0 : 0.0);
        
        return summary;
    }
    
    public List<Map<String, Object>> getIncomeProgression(Long id, String username) {
        IncomeSource incomeSource = getIncomeSourceByIdAndUser(id, username);
        return incomeSource.getYearlyIncomeProgression();
    }
    
    public Map<String, Object> getIncomeForYear(Long id, Integer targetYear, String username) {
        IncomeSource incomeSource = getIncomeSourceByIdAndUser(id, username);
        return incomeSource.calculateIncomeForYear(targetYear);
    }
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getFastestGrowingIncomes(String username, int limit) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        List<IncomeSource> incomeSources = incomeSourceRepository.findByUser(user);
        
        return incomeSources.stream()
                .sorted((is1, is2) -> Double.compare(is2.getAnnualGrowthRate(), is1.getAnnualGrowthRate()))
                .limit(limit)
                .map(is -> {
                    Map<String, Object> growthData = new HashMap<>();
                    growthData.put("id", is.getId());
                    growthData.put("sourceName", is.getSourceName());
                    growthData.put("initialMonthlyIncome", is.getInitialMonthlyIncome());
                    growthData.put("projectedMonthlyIncome", is.getProjectedMonthlyIncome());
                    growthData.put("annualGrowthRate", is.getAnnualGrowthRate());
                    growthData.put("totalGrowth", Math.round(((is.getProjectedMonthlyIncome() - is.getInitialMonthlyIncome()) / is.getInitialMonthlyIncome()) * 100 * 100.0) / 100.0);
                    growthData.put("years", is.getYears());
                    return growthData;
                })
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getYearlyIncomeSummary(String username, int targetYear) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        List<IncomeSource> incomeSources = incomeSourceRepository.findByUser(user);
        
        double totalMonthlyIncome = 0.0;
        double totalAnnualIncome = 0.0;
        List<Map<String, Object>> sourceDetails = new ArrayList<>();
        
        for (IncomeSource incomeSource : incomeSources) {
            if (targetYear <= incomeSource.getYears()) {
                Map<String, Object> yearData = incomeSource.calculateIncomeForYear(targetYear);
                double monthlyIncome = (Double) yearData.get("monthlyIncome");
                double annualIncome = (Double) yearData.get("annualIncome");
                
                totalMonthlyIncome += monthlyIncome;
                totalAnnualIncome += annualIncome;
                
                Map<String, Object> sourceDetail = new HashMap<>();
                sourceDetail.put("sourceName", incomeSource.getSourceName());
                sourceDetail.put("monthlyIncome", monthlyIncome);
                sourceDetail.put("annualIncome", annualIncome);
                sourceDetail.put("growthRate", incomeSource.getAnnualGrowthRate());
                sourceDetails.add(sourceDetail);
            }
        }
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("year", targetYear);
        summary.put("totalMonthlyIncome", Math.round(totalMonthlyIncome * 100.0) / 100.0);
        summary.put("totalAnnualIncome", Math.round(totalAnnualIncome * 100.0) / 100.0);
        summary.put("incomeSources", sourceDetails);
        
        return summary;
    }
}
```

## 4. IncomeSource Controller

### IncomeSourceController.java
```java
@RestController
@RequestMapping("/api/income-sources")
public class IncomeSourceController {
    
    @Autowired
    private IncomeSourceService incomeSourceService;
    
    @PostMapping
    public ResponseEntity<?> createIncomeSource(@RequestBody IncomeSource incomeSource, Principal principal) {
        try {
            IncomeSource savedIncomeSource = incomeSourceService.createIncomeSource(incomeSource, principal.getName());
            return ResponseEntity.ok(savedIncomeSource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating income source: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<IncomeSource>> getUserIncomeSources(Principal principal) {
        try {
            List<IncomeSource> incomeSources = incomeSourceService.getUserIncomeSources(principal.getName());
            return ResponseEntity.ok(incomeSources);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getIncomeSourceById(@PathVariable Long id, Principal principal) {
        try {
            IncomeSource incomeSource = incomeSourceService.getIncomeSourceByIdAndUser(id, principal.getName());
            return ResponseEntity.ok(incomeSource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateIncomeSource(@PathVariable Long id, @RequestBody IncomeSource incomeSource, Principal principal) {
        try {
            IncomeSource updatedIncomeSource = incomeSourceService.updateIncomeSource(id, incomeSource, principal.getName());
            return ResponseEntity.ok(updatedIncomeSource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating income source: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteIncomeSource(@PathVariable Long id, Principal principal) {
        try {
            incomeSourceService.deleteIncomeSource(id, principal.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting income source: " + e.getMessage());
        }
    }
    
    @GetMapping("/summary/overview")
    public ResponseEntity<Map<String, Object>> getIncomeSourceSummary(Principal principal) {
        try {
            Map<String, Object> summary = incomeSourceService.getIncomeSourceSummary(principal.getName());
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyMap());
        }
    }
    
    @GetMapping("/{id}/progression")
    public ResponseEntity<?> getIncomeProgression(@PathVariable Long id, Principal principal) {
        try {
            List<Map<String, Object>> progression = incomeSourceService.getIncomeProgression(id, principal.getName());
            return ResponseEntity.ok(progression);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating income progression: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}/yearly-income")
    public ResponseEntity<?> getIncomeForYear(
            @PathVariable Long id, 
            @RequestParam Integer targetYear,
            Principal principal) {
        try {
            Map<String, Object> incomeData = incomeSourceService.getIncomeForYear(id, targetYear, principal.getName());
            return ResponseEntity.ok(incomeData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error calculating yearly income: " + e.getMessage());
        }
    }
    
    @GetMapping("/fastest-growing")
    public ResponseEntity<?> getFastestGrowingIncomes(
            @RequestParam(defaultValue = "5") int limit,
            Principal principal) {
        try {
            List<Map<String, Object>> fastestGrowing = incomeSourceService.getFastestGrowingIncomes(principal.getName(), limit);
            return ResponseEntity.ok(fastestGrowing);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error getting fastest growing incomes: " + e.getMessage());
        }
    }
    
    @GetMapping("/yearly-summary")
    public ResponseEntity<?> getYearlyIncomeSummary(
            @RequestParam Integer targetYear,
            Principal principal) {
        try {
            Map<String, Object> yearlySummary = incomeSourceService.getYearlyIncomeSummary(principal.getName(), targetYear);
            return ResponseEntity.ok(yearlySummary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating yearly summary: " + e.getMessage());
        }
    }
}
```

## 5. Angular IncomeSource Model

### income-source.model.ts
```typescript
export interface IncomeSource {
  id?: number;
  sourceName: string;
  initialMonthlyIncome: number | null;
  annualGrowthRate: number | null;
  years: number | null;
  projectedMonthlyIncome: number | null;
  totalAmountReceived: number | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface IncomeSourceSummary {
  totalIncomeSources: number;
  totalInitialMonthlyIncome: number;
  totalProjectedMonthlyIncome: number;
  totalAmountReceived: number;
  averageGrowthRate: number;
  totalGrowthPercentage: number;
}

export interface IncomeProgression {
  year: number;
  monthlyIncome: number;
  annualIncome: number;
  cumulativeIncome: number;
  growthFromStart: number;
}

export interface YearlyIncome {
  year: number;
  monthlyIncome: number;
  annualIncome: number;
  growthFromPrevious: number;
}

export interface FastestGrowingIncome {
  id: number;
  sourceName: string;
  initialMonthlyIncome: number;
  projectedMonthlyIncome: number;
  annualGrowthRate: number;
  totalGrowth: number;
  years: number;
}

export interface YearlyIncomeSummary {
  year: number;
  totalMonthlyIncome: number;
  totalAnnualIncome: number;
  incomeSources: Array<{
    sourceName: string;
    monthlyIncome: number;
    annualIncome: number;
    growthRate: number;
  }>;
}
```

## 6. Angular IncomeSource Service

### income-source.service.ts
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { 
  IncomeSource, 
  IncomeSourceSummary, 
  IncomeProgression, 
  YearlyIncome,
  FastestGrowingIncome,
  YearlyIncomeSummary 
} from '../models/income-source.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class IncomeSourceService {
  private apiUrl = 'http://localhost:8080/api/income-sources';

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

  createIncomeSource(incomeSource: IncomeSource): Observable<IncomeSource> {
    return this.http.post<IncomeSource>(this.apiUrl, incomeSource, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getUserIncomeSources(): Observable<IncomeSource[]> {
    return this.http.get<IncomeSource[]>(this.apiUrl, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getIncomeSourceById(id: number): Observable<IncomeSource> {
    return this.http.get<IncomeSource>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  updateIncomeSource(id: number, incomeSource: IncomeSource): Observable<IncomeSource> {
    return this.http.put<IncomeSource>(`${this.apiUrl}/${id}`, incomeSource, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  deleteIncomeSource(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getIncomeSourceSummary(): Observable<IncomeSourceSummary> {
    return this.http.get<IncomeSourceSummary>(`${this.apiUrl}/summary/overview`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getIncomeProgression(id: number): Observable<IncomeProgression[]> {
    return this.http.get<IncomeProgression[]>(`${this.apiUrl}/${id}/progression`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getIncomeForYear(id: number, targetYear: number): Observable<YearlyIncome> {
    const params = new HttpParams().set('targetYear', targetYear.toString());
    return this.http.get<YearlyIncome>(`${this.apiUrl}/${id}/yearly-income`, { 
      headers: this.getAuthHeaders(),
      params: params
    }).pipe(catchError(this.handleError));
  }

  getFastestGrowingIncomes(limit: number = 5): Observable<FastestGrowingIncome[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<FastestGrowingIncome[]>(`${this.apiUrl}/fastest-growing`, { 
      headers: this.getAuthHeaders(),
      params: params
    }).pipe(catchError(this.handleError));
  }

  getYearlyIncomeSummary(targetYear: number): Observable<YearlyIncomeSummary> {
    const params = new HttpParams().set('targetYear', targetYear.toString());
    return this.http.get<YearlyIncomeSummary>(`${this.apiUrl}/yearly-summary`, { 
      headers: this.getAuthHeaders(),
      params: params
    }).pipe(catchError(this.handleError));
  }
}
```

## 7. IncomeSource List Component

### income-source-list.component.ts
```typescript
import { Component, OnInit } from '@angular/core';
import { IncomeSource, IncomeSourceSummary, FastestGrowingIncome } from '../../models/income-source.model';
import { IncomeSourceService } from '../../services/income-source.service';

@Component({
  selector: 'app-income-source-list',
  templateUrl: './income-source-list.component.html',
  styleUrls: ['./income-source-list.component.css']
})
export class IncomeSourceListComponent implements OnInit {
  incomeSources: IncomeSource[] = [];
  summary: IncomeSourceSummary | null = null;
  fastestGrowing: FastestGrowingIncome[] = [];
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(private incomeSourceService: IncomeSourceService) { }

  ngOnInit(): void {
    this.loadIncomeSources();
    this.loadSummary();
    this.loadFastestGrowing();
  }

  loadIncomeSources(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.incomeSourceService.getUserIncomeSources().subscribe({
      next: (incomeSources) => {
        this.incomeSources = incomeSources;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load income sources. Please try again.';
        this.isLoading = false;
        console.error('Error loading income sources:', error);
      }
    });
  }

  loadSummary(): void {
    this.incomeSourceService.getIncomeSourceSummary().subscribe({
      next: (summary) => {
        this.summary = summary;
      },
      error: (error) => {
        console.error('Error loading income source summary:', error);
      }
    });
  }

  loadFastestGrowing(): void {
    this.incomeSourceService.getFastestGrowingIncomes(5).subscribe({
      next: (fastestGrowing) => {
        this.fastestGrowing = fastestGrowing;
      },
      error: (error) => {
        console.error('Error loading fastest growing incomes:', error);
      }
    });
  }

  deleteIncomeSource(id: number): void {
    if (confirm('Are you sure you want to delete this income source?')) {
      this.incomeSourceService.deleteIncomeSource(id).subscribe({
        next: () => {
          this.incomeSources = this.incomeSources.filter(is => is.id !== id);
          this.loadSummary();
          this.loadFastestGrowing();
        },
        error: (error) => {
          this.errorMessage = 'Failed to delete income source. Please try again.';
          console.error('Error deleting income source:', error);
        }
      });
    }
  }

  calculateTotalGrowth(incomeSource: IncomeSource): number {
    if (!incomeSource.initialMonthlyIncome || !incomeSource.projectedMonthlyIncome) return 0;
    return ((incomeSource.projectedMonthlyIncome - incomeSource.initialMonthlyIncome) / incomeSource.initialMonthlyIncome) * 100;
  }

  getAnnualIncome(incomeSource: IncomeSource): number {
    return incomeSource.initialMonthlyIncome ? incomeSource.initialMonthlyIncome * 12 : 0;
  }

  getProjectedAnnualIncome(incomeSource: IncomeSource): number {
    return incomeSource.projectedMonthlyIncome ? incomeSource.projectedMonthlyIncome * 12 : 0;
  }
}
```

## 8. IncomeSource Form Component

### income-source-form.component.ts
```typescript
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { IncomeSourceService } from '../../services/income-source.service';
import { IncomeSource } from '../../models/income-source.model';

@Component({
  selector: 'app-income-source-form',
  templateUrl: './income-source-form.component.html',
  styleUrls: ['./income-source-form.component.css']
})
export class IncomeSourceFormComponent implements OnInit {
  incomeSourceForm: FormGroup;
  isEdit = false;
  incomeSourceId?: number;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  calculatedValues: any = {};

  constructor(
    private fb: FormBuilder,
    private incomeSourceService: IncomeSourceService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.incomeSourceForm = this.createForm();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEdit = true;
        this.incomeSourceId = +params['id'];
        this.loadIncomeSource(this.incomeSourceId);
      }
    });

    // Recalculate when form values change
    this.incomeSourceForm.valueChanges.subscribe(() => {
      this.calculateIncomeProjection();
    });
  }

  createForm(): FormGroup {
    return this.fb.group({
      sourceName: ['', [Validators.required, Validators.maxLength(100)]],
      initialMonthlyIncome: [null, [Validators.required, Validators.min(1), Validators.max(1000000)]],
      annualGrowthRate: [null, [Validators.required, Validators.min(0), Validators.max(100)]],
      years: [null, [Validators.required, Validators.min(1), Validators.max(50)]]
    });
  }

  loadIncomeSource(id: number): void {
    this.isLoading = true;
    this.incomeSourceService.getIncomeSourceById(id).subscribe({
      next: (incomeSource) => {
        this.incomeSourceForm.patchValue({
          sourceName: incomeSource.sourceName,
          initialMonthlyIncome: incomeSource.initialMonthlyIncome,
          annualGrowthRate: incomeSource.annualGrowthRate,
          years: incomeSource.years
        });
        this.calculatedValues = {
          projectedMonthlyIncome: incomeSource.projectedMonthlyIncome,
          totalAmountReceived: incomeSource.totalAmountReceived
        };
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load income source. Please try again.';
        this.isLoading = false;
        console.error('Error loading income source:', error);
      }
    });
  }

  calculateIncomeProjection(): void {
    const formValue = this.incomeSourceForm.value;
    if (formValue.initialMonthlyIncome && formValue.annualGrowthRate && formValue.years) {
      const initialIncome = formValue.initialMonthlyIncome;
      const growthRate = formValue.annualGrowthRate / 100;
      const years = formValue.years;

      // Calculate projected monthly income
      const projectedMonthlyIncome = initialIncome * Math.pow(1 + growthRate, years);
      
      // Calculate total amount received over the years
      let totalAmount = 0;
      let currentYearIncome = initialIncome * 12; // Annual income for first year
      
      for (let year = 1; year <= years; year++) {
        totalAmount += currentYearIncome;
        currentYearIncome *= (1 + growthRate); // Grow income for next year
      }

      const totalGrowth = ((projectedMonthlyIncome - initialIncome) / initialIncome) * 100;

      this.calculatedValues = {
        projectedMonthlyIncome: Math.round(projectedMonthlyIncome * 100) / 100,
        totalAmountReceived: Math.round(totalAmount * 100) / 100,
        totalGrowth: Math.round(totalGrowth * 100) / 100,
        projectedAnnualIncome: Math.round(projectedMonthlyIncome * 12 * 100) / 100
      };
    } else {
      this.calculatedValues = {};
    }
  }

  onSubmit(): void {
    if (this.incomeSourceForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const incomeSource: IncomeSource = this.incomeSourceForm.value;

      const operation = this.isEdit && this.incomeSourceId
        ? this.incomeSourceService.updateIncomeSource(this.incomeSourceId, incomeSource)
        : this.incomeSourceService.createIncomeSource(incomeSource);

      operation.subscribe({
        next: (savedIncomeSource) => {
          this.isLoading = false;
          this.successMessage = this.isEdit 
            ? 'Income source updated successfully!' 
            : 'Income source created successfully!';
          
          setTimeout(() => {
            this.router.navigate(['/income-sources']);
          }, 1500);
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = this.isEdit
            ? 'Failed to update income source. Please try again.'
            : 'Failed to create income source. Please try again.';
          console.error('Error saving income source:', error);
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  markFormGroupTouched(): void {
    Object.keys(this.incomeSourceForm.controls).forEach(key => {
      const control = this.incomeSourceForm.get(key);
      control?.markAsTouched();
    });
  }

  onCancel(): void {
    if (this.incomeSourceForm.dirty) {
      if (confirm('You have unsaved changes. Are you sure you want to leave?')) {
        this.router.navigate(['/income-sources']);
      }
    } else {
      this.router.navigate(['/income-sources']);
    }
  }

  // Getters for easy access in template
  get sourceName() { return this.incomeSourceForm.get('sourceName'); }
  get initialMonthlyIncome() { return this.incomeSourceForm.get('initialMonthlyIncome'); }
  get annualGrowthRate() { return this.incomeSourceForm.get('annualGrowthRate'); }
  get years() { return this.incomeSourceForm.get('years'); }
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
    IncomeSourceFormComponent
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

# Create Income Source
curl -X POST http://localhost:8080/api/income-sources \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "sourceName": "Salary",
    "initialMonthlyIncome": 5000,
    "annualGrowthRate": 5,
    "years": 10
  }'

# Get all income sources
curl -X GET http://localhost:8080/api/income-sources \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get income source summary
curl -X GET http://localhost:8080/api/income-sources/summary/overview \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get income progression
curl -X GET http://localhost:8080/api/income-sources/1/progression \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get income for specific year
curl -X GET "http://localhost:8080/api/income-sources/1/yearly-income?targetYear=5" \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get fastest growing incomes
curl -X GET "http://localhost:8080/api/income-sources/fastest-growing?limit=3" \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get yearly income summary
curl -X GET "http://localhost:8080/api/income-sources/yearly-summary?targetYear=5" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## Key Features:

1. **Income Growth Projections**: Automatic calculation of future income with annual growth
2. **Yearly Progression**: Detailed year-by-year income growth tracking
3. **Total Earnings Calculation**: Cumulative income received over the projection period
4. **Growth Analytics**: Track growth rates and performance metrics
5. **Fastest Growing Identification**: Identify income sources with highest growth potential
6. **Yearly Summaries**: Comprehensive income summaries for any target year
7. **Real-time Calculations**: Automatic updates as input values change
8. **User-specific Data**: Secure, personalized income source management

The Income Source management is now fully integrated with your Spring Boot backend and Angular frontend, completing your comprehensive financial management system with Expenses, EMIs, SIPs, LumpSum investments, and Income Sources!