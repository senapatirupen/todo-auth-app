now integrate this details to spring application. export interface InvestmentOption {
id?: number;
name: string;
category: 'equity' | 'fixed-income' | 'real-estate' | 'commodities' | 'alternative';
minCAGR: number;
maxCAGR: number;
riskLevel: 'low' | 'medium' | 'high' | 'very-high';
liquidity: 'high' | 'medium' | 'low';
taxEfficiency?: string;
notes?: string;
} like you integrate expense management

I'll integrate the Investment Option management functionality into your Spring Boot application. Here's the complete implementation:

## 1. InvestmentOption Entity

### InvestmentOption.java
```java
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
```

## 2. InvestmentOption Repository

### InvestmentOptionRepository.java
```java
@Repository
public interface InvestmentOptionRepository extends JpaRepository<InvestmentOption, Long> {
    List<InvestmentOption> findByUser(User user);
    List<InvestmentOption> findByUserOrderByCreatedAtDesc(User user);
    Optional<InvestmentOption> findByIdAndUser(Long id, User user);
    void deleteByIdAndUser(Long id, User user);
    
    List<InvestmentOption> findByUserAndCategory(User user, InvestmentOption.InvestmentCategory category);
    List<InvestmentOption> findByUserAndRiskLevel(User user, InvestmentOption.RiskLevel riskLevel);
    List<InvestmentOption> findByUserAndLiquidity(User user, InvestmentOption.Liquidity liquidity);
    
    @Query("SELECT io FROM InvestmentOption io WHERE io.user = :user AND io.minCAGR >= :minCAGR AND io.maxCAGR <= :maxCAGR")
    List<InvestmentOption> findByUserAndCAGRRange(@Param("user") User user, 
                                                  @Param("minCAGR") Double minCAGR, 
                                                  @Param("maxCAGR") Double maxCAGR);
    
    @Query("SELECT COUNT(io) FROM InvestmentOption io WHERE io.user = :user AND io.category = :category")
    Long countByUserAndCategory(@Param("user") User user, 
                               @Param("category") InvestmentOption.InvestmentCategory category);
    
    @Query("SELECT AVG((io.minCAGR + io.maxCAGR) / 2) FROM InvestmentOption io WHERE io.user = :user")
    Optional<Double> findAverageCAGRByUser(@Param("user") User user);
    
    @Query("SELECT io.category, COUNT(io) FROM InvestmentOption io WHERE io.user = :user GROUP BY io.category")
    List<Object[]> countByCategoryForUser(@Param("user") User user);
}
```

## 3. InvestmentOption Service

### InvestmentOptionService.java
```java
@Service
@Transactional
public class InvestmentOptionService {
    
    @Autowired
    private InvestmentOptionRepository investmentOptionRepository;
    
    @Autowired
    private UserService userService;
    
    public InvestmentOption createInvestmentOption(InvestmentOption investmentOption, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        investmentOption.setUser(user);
        return investmentOptionRepository.save(investmentOption);
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentOption> getUserInvestmentOptions(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        return investmentOptionRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    @Transactional(readOnly = true)
    public InvestmentOption getInvestmentOptionByIdAndUser(Long id, String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        return investmentOptionRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Investment option not found with id: " + id));
    }
    
    public InvestmentOption updateInvestmentOption(Long id, InvestmentOption investmentOptionDetails, String username) {
        InvestmentOption investmentOption = getInvestmentOptionByIdAndUser(id, username);
        
        // Update all fields
        investmentOption.setName(investmentOptionDetails.getName());
        investmentOption.setCategory(investmentOptionDetails.getCategory());
        investmentOption.setMinCAGR(investmentOptionDetails.getMinCAGR());
        investmentOption.setMaxCAGR(investmentOptionDetails.getMaxCAGR());
        investmentOption.setRiskLevel(investmentOptionDetails.getRiskLevel());
        investmentOption.setLiquidity(investmentOptionDetails.getLiquidity());
        investmentOption.setTaxEfficiency(investmentOptionDetails.getTaxEfficiency());
        investmentOption.setNotes(investmentOptionDetails.getNotes());
        
        return investmentOptionRepository.save(investmentOption);
    }
    
    public void deleteInvestmentOption(Long id, String username) {
        InvestmentOption investmentOption = getInvestmentOptionByIdAndUser(id, username);
        investmentOptionRepository.delete(investmentOption);
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentOption> getInvestmentOptionsByCategory(String username, InvestmentOption.InvestmentCategory category) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        return investmentOptionRepository.findByUserAndCategory(user, category);
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentOption> getInvestmentOptionsByRiskLevel(String username, InvestmentOption.RiskLevel riskLevel) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        return investmentOptionRepository.findByUserAndRiskLevel(user, riskLevel);
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentOption> getInvestmentOptionsByLiquidity(String username, InvestmentOption.Liquidity liquidity) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        return investmentOptionRepository.findByUserAndLiquidity(user, liquidity);
    }
    
    @Transactional(readOnly = true)
    public List<InvestmentOption> getInvestmentOptionsByCAGRRange(String username, Double minCAGR, Double maxCAGR) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        return investmentOptionRepository.findByUserAndCAGRRange(user, minCAGR, maxCAGR);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getInvestmentOptionsSummary(String username) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        List<InvestmentOption> investmentOptions = investmentOptionRepository.findByUser(user);
        
        double averageCAGR = investmentOptionRepository.findAverageCAGRByUser(user).orElse(0.0);
        
        // Count by category
        Map<InvestmentOption.InvestmentCategory, Long> categoryCounts = new HashMap<>();
        List<Object[]> categoryCountsResult = investmentOptionRepository.countByCategoryForUser(user);
        for (Object[] result : categoryCountsResult) {
            InvestmentOption.InvestmentCategory category = (InvestmentOption.InvestmentCategory) result[0];
            Long count = (Long) result[1];
            categoryCounts.put(category, count);
        }
        
        // Count by risk level
        Map<InvestmentOption.RiskLevel, Long> riskLevelCounts = investmentOptions.stream()
                .collect(Collectors.groupingBy(InvestmentOption::getRiskLevel, Collectors.counting()));
        
        // Count by liquidity
        Map<InvestmentOption.Liquidity, Long> liquidityCounts = investmentOptions.stream()
                .collect(Collectors.groupingBy(InvestmentOption::getLiquidity, Collectors.counting()));
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalInvestmentOptions", investmentOptions.size());
        summary.put("averageCAGR", Math.round(averageCAGR * 100.0) / 100.0);
        summary.put("categoryDistribution", categoryCounts);
        summary.put("riskDistribution", riskLevelCounts);
        summary.put("liquidityDistribution", liquidityCounts);
        
        return summary;
    }
    
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecommendedOptions(String username, 
                                                         InvestmentOption.RiskLevel preferredRiskLevel,
                                                         InvestmentOption.Liquidity preferredLiquidity,
                                                         Double minExpectedCAGR) {
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        
        List<InvestmentOption> allOptions = investmentOptionRepository.findByUser(user);
        
        return allOptions.stream()
                .filter(option -> option.getRiskLevel().ordinal() <= preferredRiskLevel.ordinal())
                .filter(option -> option.getLiquidity().ordinal() >= preferredLiquidity.ordinal())
                .filter(option -> option.getAverageCAGR() >= minExpectedCAGR)
                .sorted((o1, o2) -> Double.compare(o2.getAverageCAGR(), o1.getAverageCAGR()))
                .map(option -> {
                    Map<String, Object> recommendation = new HashMap<>();
                    recommendation.put("id", option.getId());
                    recommendation.put("name", option.getName());
                    recommendation.put("category", option.getCategory());
                    recommendation.put("averageCAGR", Math.round(option.getAverageCAGR() * 100.0) / 100.0);
                    recommendation.put("riskLevel", option.getRiskLevel());
                    recommendation.put("liquidity", option.getLiquidity());
                    recommendation.put("matchScore", calculateMatchScore(option, preferredRiskLevel, preferredLiquidity, minExpectedCAGR));
                    return recommendation;
                })
                .collect(Collectors.toList());
    }
    
    private int calculateMatchScore(InvestmentOption option, 
                                  InvestmentOption.RiskLevel preferredRiskLevel,
                                  InvestmentOption.Liquidity preferredLiquidity,
                                  Double minExpectedCAGR) {
        int score = 0;
        
        // Risk score (lower risk is better if within preferred range)
        if (option.getRiskLevel().ordinal() <= preferredRiskLevel.ordinal()) {
            score += (preferredRiskLevel.ordinal() - option.getRiskLevel().ordinal()) * 10;
        }
        
        // Liquidity score (higher liquidity is better)
        if (option.getLiquidity().ordinal() >= preferredLiquidity.ordinal()) {
            score += (option.getLiquidity().ordinal() - preferredLiquidity.ordinal()) * 5;
        }
        
        // CAGR score (higher CAGR is better)
        if (option.getAverageCAGR() >= minExpectedCAGR) {
            score += (int) ((option.getAverageCAGR() - minExpectedCAGR) * 20);
        }
        
        return score;
    }
}
```

## 4. InvestmentOption Controller

### InvestmentOptionController.java
```java
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
```

## 5. Angular InvestmentOption Model

### investment-option.model.ts
```typescript
export interface InvestmentOption {
  id?: number;
  name: string;
  category: 'EQUITY' | 'FIXED_INCOME' | 'REAL_ESTATE' | 'COMMODITIES' | 'ALTERNATIVE';
  minCAGR: number;
  maxCAGR: number;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'VERY_HIGH';
  liquidity: 'HIGH' | 'MEDIUM' | 'LOW';
  taxEfficiency?: string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface InvestmentOptionSummary {
  totalInvestmentOptions: number;
  averageCAGR: number;
  categoryDistribution: { [key: string]: number };
  riskDistribution: { [key: string]: number };
  liquidityDistribution: { [key: string]: number };
}

export interface InvestmentRecommendation {
  id: number;
  name: string;
  category: string;
  averageCAGR: number;
  riskLevel: string;
  liquidity: string;
  matchScore: number;
}

export interface CategoryStats {
  category: string;
  count: number;
  percentage: number;
}
```

## 6. Angular InvestmentOption Service

### investment-option.service.ts
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { 
  InvestmentOption, 
  InvestmentOptionSummary, 
  InvestmentRecommendation 
} from '../models/investment-option.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class InvestmentOptionService {
  private apiUrl = 'http://localhost:8080/api/investment-options';

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

  createInvestmentOption(investmentOption: InvestmentOption): Observable<InvestmentOption> {
    return this.http.post<InvestmentOption>(this.apiUrl, investmentOption, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getUserInvestmentOptions(): Observable<InvestmentOption[]> {
    return this.http.get<InvestmentOption[]>(this.apiUrl, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getInvestmentOptionById(id: number): Observable<InvestmentOption> {
    return this.http.get<InvestmentOption>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  updateInvestmentOption(id: number, investmentOption: InvestmentOption): Observable<InvestmentOption> {
    return this.http.put<InvestmentOption>(`${this.apiUrl}/${id}`, investmentOption, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  deleteInvestmentOption(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getInvestmentOptionsByCategory(category: string): Observable<InvestmentOption[]> {
    return this.http.get<InvestmentOption[]>(`${this.apiUrl}/category/${category}`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getInvestmentOptionsByRiskLevel(riskLevel: string): Observable<InvestmentOption[]> {
    return this.http.get<InvestmentOption[]>(`${this.apiUrl}/risk-level/${riskLevel}`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getInvestmentOptionsByLiquidity(liquidity: string): Observable<InvestmentOption[]> {
    return this.http.get<InvestmentOption[]>(`${this.apiUrl}/liquidity/${liquidity}`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getInvestmentOptionsByCAGRRange(minCAGR: number, maxCAGR: number): Observable<InvestmentOption[]> {
    const params = new HttpParams()
      .set('minCAGR', minCAGR.toString())
      .set('maxCAGR', maxCAGR.toString());
    return this.http.get<InvestmentOption[]>(`${this.apiUrl}/cagr-range`, { 
      headers: this.getAuthHeaders(),
      params: params
    }).pipe(catchError(this.handleError));
  }

  getInvestmentOptionsSummary(): Observable<InvestmentOptionSummary> {
    return this.http.get<InvestmentOptionSummary>(`${this.apiUrl}/summary/overview`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getRecommendedOptions(preferredRiskLevel: string, preferredLiquidity: string, minExpectedCAGR: number): Observable<InvestmentRecommendation[]> {
    const params = new HttpParams()
      .set('preferredRiskLevel', preferredRiskLevel)
      .set('preferredLiquidity', preferredLiquidity)
      .set('minExpectedCAGR', minExpectedCAGR.toString());
    return this.http.get<InvestmentRecommendation[]>(`${this.apiUrl}/recommendations`, { 
      headers: this.getAuthHeaders(),
      params: params
    }).pipe(catchError(this.handleError));
  }
}
```

## 7. InvestmentOption List Component

### investment-option-list.component.ts
```typescript
import { Component, OnInit } from '@angular/core';
import { InvestmentOption, InvestmentOptionSummary, InvestmentRecommendation } from '../../models/investment-option.model';
import { InvestmentOptionService } from '../../services/investment-option.service';

@Component({
  selector: 'app-investment-option-list',
  templateUrl: './investment-option-list.component.html',
  styleUrls: ['./investment-option-list.component.css']
})
export class InvestmentOptionListComponent implements OnInit {
  investmentOptions: InvestmentOption[] = [];
  summary: InvestmentOptionSummary | null = null;
  recommendations: InvestmentRecommendation[] = [];
  isLoading: boolean = false;
  errorMessage: string = '';
  
  // Filter properties
  selectedCategory: string = '';
  selectedRiskLevel: string = '';
  selectedLiquidity: string = '';
  minCAGR: number = 0;
  maxCAGR: number = 50;

  // Recommendation preferences
  preferredRiskLevel: string = 'MEDIUM';
  preferredLiquidity: string = 'HIGH';
  minExpectedCAGR: number = 8;

  // Categories and enums for dropdowns
  categories = ['EQUITY', 'FIXED_INCOME', 'REAL_ESTATE', 'COMMODITIES', 'ALTERNATIVE'];
  riskLevels = ['LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH'];
  liquidityLevels = ['HIGH', 'MEDIUM', 'LOW'];

  constructor(private investmentOptionService: InvestmentOptionService) { }

  ngOnInit(): void {
    this.loadInvestmentOptions();
    this.loadSummary();
  }

  loadInvestmentOptions(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.investmentOptionService.getUserInvestmentOptions().subscribe({
      next: (options) => {
        this.investmentOptions = options;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load investment options. Please try again.';
        this.isLoading = false;
        console.error('Error loading investment options:', error);
      }
    });
  }

  loadSummary(): void {
    this.investmentOptionService.getInvestmentOptionsSummary().subscribe({
      next: (summary) => {
        this.summary = summary;
      },
      error: (error) => {
        console.error('Error loading investment options summary:', error);
      }
    });
  }

  loadRecommendations(): void {
    this.investmentOptionService.getRecommendedOptions(
      this.preferredRiskLevel, 
      this.preferredLiquidity, 
      this.minExpectedCAGR
    ).subscribe({
      next: (recommendations) => {
        this.recommendations = recommendations;
      },
      error: (error) => {
        console.error('Error loading recommendations:', error);
      }
    });
  }

  deleteInvestmentOption(id: number): void {
    if (confirm('Are you sure you want to delete this investment option?')) {
      this.investmentOptionService.deleteInvestmentOption(id).subscribe({
        next: () => {
          this.investmentOptions = this.investmentOptions.filter(option => option.id !== id);
          this.loadSummary();
        },
        error: (error) => {
          this.errorMessage = 'Failed to delete investment option. Please try again.';
          console.error('Error deleting investment option:', error);
        }
      });
    }
  }

  applyFilters(): void {
    this.isLoading = true;
    
    if (this.selectedCategory) {
      this.investmentOptionService.getInvestmentOptionsByCategory(this.selectedCategory).subscribe({
        next: (options) => {
          this.investmentOptions = options;
          this.applyAdditionalFilters();
        },
        error: (error) => {
          this.errorMessage = 'Error applying category filter.';
          this.isLoading = false;
        }
      });
    } else if (this.selectedRiskLevel) {
      this.investmentOptionService.getInvestmentOptionsByRiskLevel(this.selectedRiskLevel).subscribe({
        next: (options) => {
          this.investmentOptions = options;
          this.applyAdditionalFilters();
        },
        error: (error) => {
          this.errorMessage = 'Error applying risk level filter.';
          this.isLoading = false;
        }
      });
    } else if (this.selectedLiquidity) {
      this.investmentOptionService.getInvestmentOptionsByLiquidity(this.selectedLiquidity).subscribe({
        next: (options) => {
          this.investmentOptions = options;
          this.applyAdditionalFilters();
        },
        error: (error) => {
          this.errorMessage = 'Error applying liquidity filter.';
          this.isLoading = false;
        }
      });
    } else {
      this.applyAdditionalFilters();
    }
  }

  applyAdditionalFilters(): void {
    this.investmentOptions = this.investmentOptions.filter(option => 
      option.minCAGR >= this.minCAGR && option.maxCAGR <= this.maxCAGR
    );
    this.isLoading = false;
  }

  clearFilters(): void {
    this.selectedCategory = '';
    this.selectedRiskLevel = '';
    this.selectedLiquidity = '';
    this.minCAGR = 0;
    this.maxCAGR = 50;
    this.loadInvestmentOptions();
  }

  getAverageCAGR(option: InvestmentOption): number {
    return (option.minCAGR + option.maxCAGR) / 2;
  }

  getRiskColor(riskLevel: string): string {
    switch (riskLevel) {
      case 'LOW': return 'success';
      case 'MEDIUM': return 'warning';
      case 'HIGH': return 'danger';
      case 'VERY_HIGH': return 'dark';
      default: return 'secondary';
    }
  }

  getLiquidityIcon(liquidity: string): string {
    switch (liquidity) {
      case 'HIGH': return 'fa-tachometer-alt-fast';
      case 'MEDIUM': return 'fa-tachometer-alt-average';
      case 'LOW': return 'fa-tachometer-alt-slow';
      default: return 'fa-tachometer-alt';
    }
  }

  getCategoryIcon(category: string): string {
    switch (category) {
      case 'EQUITY': return 'fa-chart-line';
      case 'FIXED_INCOME': return 'fa-hand-holding-usd';
      case 'REAL_ESTATE': return 'fa-home';
      case 'COMMODITIES': return 'fa-gem';
      case 'ALTERNATIVE': return 'fa-lightbulb';
      default: return 'fa-chart-pie';
    }
  }

  getCategoryStats(): any[] {
    if (!this.summary?.categoryDistribution) return [];
    
    return Object.entries(this.summary.categoryDistribution).map(([category, count]) => ({
      category,
      count,
      percentage: (count / this.summary!.totalInvestmentOptions) * 100
    }));
  }
}
```

## 8. InvestmentOption Form Component

### investment-option-form.component.ts
```typescript
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { InvestmentOptionService } from '../../services/investment-option.service';
import { InvestmentOption } from '../../models/investment-option.model';

@Component({
  selector: 'app-investment-option-form',
  templateUrl: './investment-option-form.component.html',
  styleUrls: ['./investment-option-form.component.css']
})
export class InvestmentOptionFormComponent implements OnInit {
  investmentOptionForm: FormGroup;
  isEdit = false;
  investmentOptionId?: number;
  isLoading = false;
  errorMessage = '';
  successMessage = '';

  // Dropdown options
  categories = ['EQUITY', 'FIXED_INCOME', 'REAL_ESTATE', 'COMMODITIES', 'ALTERNATIVE'];
  riskLevels = ['LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH'];
  liquidityLevels = ['HIGH', 'MEDIUM', 'LOW'];

  constructor(
    private fb: FormBuilder,
    private investmentOptionService: InvestmentOptionService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.investmentOptionForm = this.createForm();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEdit = true;
        this.investmentOptionId = +params['id'];
        this.loadInvestmentOption(this.investmentOptionId);
      }
    });
  }

  createForm(): FormGroup {
    return this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      category: ['', Validators.required],
      minCAGR: [null, [Validators.required, Validators.min(0), Validators.max(100)]],
      maxCAGR: [null, [Validators.required, Validators.min(0), Validators.max(100)]],
      riskLevel: ['', Validators.required],
      liquidity: ['', Validators.required],
      taxEfficiency: [''],
      notes: ['']
    }, { validators: this.cagrValidator });
  }

  cagrValidator(form: FormGroup) {
    const minCAGR = form.get('minCAGR')?.value;
    const maxCAGR = form.get('maxCAGR')?.value;
    
    if (minCAGR !== null && maxCAGR !== null && minCAGR > maxCAGR) {
      return { cagrRange: true };
    }
    
    return null;
  }

  loadInvestmentOption(id: number): void {
    this.isLoading = true;
    this.investmentOptionService.getInvestmentOptionById(id).subscribe({
      next: (investmentOption) => {
        this.investmentOptionForm.patchValue({
          name: investmentOption.name,
          category: investmentOption.category,
          minCAGR: investmentOption.minCAGR,
          maxCAGR: investmentOption.maxCAGR,
          riskLevel: investmentOption.riskLevel,
          liquidity: investmentOption.liquidity,
          taxEfficiency: investmentOption.taxEfficiency || '',
          notes: investmentOption.notes || ''
        });
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load investment option. Please try again.';
        this.isLoading = false;
        console.error('Error loading investment option:', error);
      }
    });
  }

  onSubmit(): void {
    if (this.investmentOptionForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const investmentOption: InvestmentOption = this.investmentOptionForm.value;

      const operation = this.isEdit && this.investmentOptionId
        ? this.investmentOptionService.updateInvestmentOption(this.investmentOptionId, investmentOption)
        : this.investmentOptionService.createInvestmentOption(investmentOption);

      operation.subscribe({
        next: (savedInvestmentOption) => {
          this.isLoading = false;
          this.successMessage = this.isEdit 
            ? 'Investment option updated successfully!' 
            : 'Investment option created successfully!';
          
          setTimeout(() => {
            this.router.navigate(['/investment-options']);
          }, 1500);
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = this.isEdit
            ? 'Failed to update investment option. Please try again.'
            : 'Failed to create investment option. Please try again.';
          console.error('Error saving investment option:', error);
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  markFormGroupTouched(): void {
    Object.keys(this.investmentOptionForm.controls).forEach(key => {
      const control = this.investmentOptionForm.get(key);
      control?.markAsTouched();
    });
  }

  onCancel(): void {
    if (this.investmentOptionForm.dirty) {
      if (confirm('You have unsaved changes. Are you sure you want to leave?')) {
        this.router.navigate(['/investment-options']);
      }
    } else {
      this.router.navigate(['/investment-options']);
    }
  }

  getAverageCAGR(): number {
    const minCAGR = this.investmentOptionForm.get('minCAGR')?.value;
    const maxCAGR = this.investmentOptionForm.get('maxCAGR')?.value;
    
    if (minCAGR !== null && maxCAGR !== null) {
      return (minCAGR + maxCAGR) / 2;
    }
    return 0;
  }

  // Getters for easy access in template
  get name() { return this.investmentOptionForm.get('name'); }
  get category() { return this.investmentOptionForm.get('category'); }
  get minCAGR() { return this.investmentOptionForm.get('minCAGR'); }
  get maxCAGR() { return this.investmentOptionForm.get('maxCAGR'); }
  get riskLevel() { return this.investmentOptionForm.get('riskLevel'); }
  get liquidity() { return this.investmentOptionForm.get('liquidity'); }
  get taxEfficiency() { return this.investmentOptionForm.get('taxEfficiency'); }
  get notes() { return this.investmentOptionForm.get('notes'); }
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
    InvestmentOptionFormComponent
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

# Create Investment Option
curl -X POST http://localhost:8080/api/investment-options \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "name": "Large Cap Mutual Funds",
    "category": "EQUITY",
    "minCAGR": 10,
    "maxCAGR": 15,
    "riskLevel": "MEDIUM",
    "liquidity": "HIGH",
    "taxEfficiency": "Equity LTCG tax after 1 year",
    "notes": "Good for long-term wealth creation"
  }'

# Get all investment options
curl -X GET http://localhost:8080/api/investment-options \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get investment options by category
curl -X GET http://localhost:8080/api/investment-options/category/EQUITY \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get investment options by risk level
curl -X GET http://localhost:8080/api/investment-options/risk-level/MEDIUM \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get investment options by CAGR range
curl -X GET "http://localhost:8080/api/investment-options/cagr-range?minCAGR=8&maxCAGR=12" \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get investment options summary
curl -X GET http://localhost:8080/api/investment-options/summary/overview \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get recommendations
curl -X GET "http://localhost:8080/api/investment-options/recommendations?preferredRiskLevel=MEDIUM&preferredLiquidity=HIGH&minExpectedCAGR=8" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## Key Features:

1. **Comprehensive Investment Catalog**: Manage different types of investment options
2. **Advanced Filtering**: Filter by category, risk level, liquidity, and CAGR range
3. **Smart Recommendations**: Get personalized investment recommendations based on preferences
4. **Risk Assessment**: Visual risk indicators and categorization
5. **Performance Metrics**: Track CAGR ranges and average returns
6. **Tax Efficiency Tracking**: Document tax implications for each investment
7. **Distribution Analytics**: View category, risk, and liquidity distributions
8. **User-specific Catalog**: Personalized investment option database

The Investment Option management is now fully integrated with your Spring Boot backend and Angular frontend, completing your comprehensive financial management system with Expenses, EMIs, SIPs, LumpSum investments, Income Sources, and Investment Options!