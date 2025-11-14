now integrate this details to spring application. export interface LumpSum {
id?: number;
investmentName: string;
principalAmount: number | null;
duration: number | null;
expectedReturn: number | null;
futureValue: number | null;
totalInterest: number | null;
} like you integrate expense management

I'll integrate the LumpSum investment management functionality into your Spring Boot application. Here's the complete implementation:

## 1. LumpSum Entity

### LumpSum.java
```java
@Entity
@Table(name = "lump_sums")
public class LumpSum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "investment_name", nullable = false)
    private String investmentName;
    
    @Column(name = "principal_amount", nullable = false)
    private Double principalAmount;
    
    @Column(name = "duration", nullable = false)
    private Integer duration; // in years
    
    @Column(name = "expected_return", nullable = false)
    private Double expectedReturn; // annual expected return percentage
    
    @Column(name = "future_value")
    private Double futureValue;
    
    @Column(name = "total_interest")
    private Double totalInterest;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public LumpSum() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public LumpSum(String investmentName, Double principalAmount, Integer duration, Double expectedReturn) {
        this();
        this.investmentName = investmentName;
        this.principalAmount = principalAmount;
        this.duration = duration;
        this.expectedReturn = expectedReturn;
        calculateLumpSum();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getInvestmentName() {
        return investmentName;
    }
    
    public void setInvestmentName(String investmentName) {
        this.investmentName = investmentName;
    }
    
    public Double getPrincipalAmount() {
        return principalAmount;
    }
    
    public void setPrincipalAmount(Double principalAmount) {
        this.principalAmount = principalAmount;
        calculateLumpSum();
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
        calculateLumpSum();
    }
    
    public Double getExpectedReturn() {
        return expectedReturn;
    }
    
    public void setExpectedReturn(Double expectedReturn) {
        this.expectedReturn = expectedReturn;
        calculateLumpSum();
    }
    
    public Double getFutureValue() {
        return futureValue;
    }
    
    public void setFutureValue(Double futureValue) {
        this.futureValue = futureValue;
    }
    
    public Double getTotalInterest() {
        return totalInterest;
    }
    
    public void setTotalInterest(Double totalInterest) {
        this.totalInterest = totalInterest;
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
    
    // LumpSum Calculation Methods
    public void calculateLumpSum() {
        if (principalAmount != null && duration != null && expectedReturn != null) {
            // Calculate future value using compound interest formula: FV = P * (1 + r)^n
            // where P = principal amount, r = annual rate, n = number of years
            double futureVal = principalAmount * Math.pow(1 + (expectedReturn / 100), duration);
            
            this.futureValue = Math.round(futureVal * 100.0) / 100.0;
            this.totalInterest = Math.round((futureValue - principalAmount) * 100.0) / 100.0;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    // Calculate values for a specific period (for progress tracking)
    public Map<String, Double> calculateForPeriod(int yearsCompleted) {
        if (principalAmount != null && expectedReturn != null && yearsCompleted <= duration) {
            // Current value after n years
            double currentValue = principalAmount * Math.pow(1 + (expectedReturn / 100), yearsCompleted);
            double interestSoFar = currentValue - principalAmount;
            double remainingYears = duration - yearsCompleted;
            
            // Projected future value from current position
            double projectedFutureValue = currentValue * Math.pow(1 + (expectedReturn / 100), remainingYears);
            
            Map<String, Double> result = new HashMap<>();
            result.put("currentValue", Math.round(currentValue * 100.0) / 100.0);
            result.put("interestSoFar", Math.round(interestSoFar * 100.0) / 100.0);
            result.put("remainingYears", (double) remainingYears);
            result.put("projectedFutureValue", Math.round(projectedFutureValue * 100.0) / 100.0);
            
            return result;
        }
        return Collections.emptyMap();
    }
    
    // Calculate year-by-year growth projection
    public List<Map<String, Object>> getYearlyProjection() {
        List<Map<String, Object>> projection = new ArrayList<>();
        
        for (int year = 0; year <= duration; year++) {
            double value = principalAmount * Math.pow(1 + (expectedReturn / 100), year);
            double interest = value - principalAmount;
            
            Map<String, Object> yearlyData = new HashMap<>();
            yearlyData.put("year", year);
            yearlyData.put("principal", Math.round(principalAmount * 100.0) / 100.0);
            yearlyData.put("interest", Math.round(interest * 100.0) / 100.0);
            yearlyData.put("totalValue", Math.round(value * 100.0) / 100.0);
            yearlyData.put("returnPercentage", Math.round((interest / principalAmount) * 100 * 100.0) / 100.0);
            
            projection.add(yearlyData);
        }
        
        return projection;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "LumpSum{" +
                "id=" + id +
                ", investmentName='" + investmentName + '\'' +
                ", principalAmount=" + principalAmount +
                ", duration=" + duration +
                ", expectedReturn=" + expectedReturn +
                ", futureValue=" + futureValue +
                ", totalInterest=" + totalInterest +
                '}';
    }
}
```

## 2. LumpSum Repository

### LumpSumRepository.java
```java
@Repository
public interface LumpSumRepository extends JpaRepository<LumpSum, Long> {
    List<LumpSum> findByUser(User user);
    List<LumpSum> findByUserOrderByCreatedAtDesc(User user);
    Optional<LumpSum> findByIdAndUser(Long id, User user);
    void deleteByIdAndUser(Long id, User user);
    
    @Query("SELECT SUM(ls.principalAmount) FROM LumpSum ls WHERE ls.user = :user")
    Optional<Double> findTotalPrincipalByUser(@Param("user") User user);
    
    @Query("SELECT SUM(ls.futureValue) FROM LumpSum ls WHERE ls.user = :user")
    Optional<Double> findTotalFutureValueByUser(@Param("user") User user);
    
    @Query("SELECT SUM(ls.totalInterest) FROM LumpSum ls WHERE ls.user = :user")
    Optional<Double> findTotalInterestByUser(@Param("user") User user);
    
    @Query("SELECT AVG(ls.expectedReturn) FROM LumpSum ls WHERE ls.user = :user")
    Optional<Double> findAverageReturnByUser(@Param("user") User user);
}
```

## 3. LumpSum Service

### LumpSumService.java
```java
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
        return lumpSum.calculateForPeriod(yearsCompleted);
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
```

## 4. LumpSum Controller

### LumpSumController.java
```java
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
```

## 5. Angular LumpSum Model

### lumpsum.model.ts
```typescript
export interface LumpSum {
  id?: number;
  investmentName: string;
  principalAmount: number | null;
  duration: number | null;
  expectedReturn: number | null;
  futureValue: number | null;
  totalInterest: number | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface LumpSumSummary {
  totalLumpSums: number;
  totalPrincipal: number;
  totalFutureValue: number;
  totalInterest: number;
  averageReturn: number;
  totalReturnPercentage: number;
}

export interface LumpSumProgress {
  currentValue: number;
  interestSoFar: number;
  remainingYears: number;
  projectedFutureValue: number;
}

export interface LumpSumProjection {
  year: number;
  principal: number;
  interest: number;
  totalValue: number;
  returnPercentage: number;
}

export interface TopPerformer {
  id: number;
  investmentName: string;
  principalAmount: number;
  futureValue: number;
  totalReturn: number;
  returnPercentage: number;
  duration: number;
}
```

## 6. Angular LumpSum Service

### lumpsum.service.ts
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { LumpSum, LumpSumSummary, LumpSumProgress, LumpSumProjection, TopPerformer } from '../models/lumpsum.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class LumpSumService {
  private apiUrl = 'http://localhost:8080/api/lump-sums';

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

  createLumpSum(lumpSum: LumpSum): Observable<LumpSum> {
    return this.http.post<LumpSum>(this.apiUrl, lumpSum, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getUserLumpSums(): Observable<LumpSum[]> {
    return this.http.get<LumpSum[]>(this.apiUrl, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getLumpSumById(id: number): Observable<LumpSum> {
    return this.http.get<LumpSum>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  updateLumpSum(id: number, lumpSum: LumpSum): Observable<LumpSum> {
    return this.http.put<LumpSum>(`${this.apiUrl}/${id}`, lumpSum, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  deleteLumpSum(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getLumpSumSummary(): Observable<LumpSumSummary> {
    return this.http.get<LumpSumSummary>(`${this.apiUrl}/summary/overview`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getLumpSumProgress(id: number, yearsCompleted: number): Observable<LumpSumProgress> {
    const params = new HttpParams().set('yearsCompleted', yearsCompleted.toString());
    return this.http.get<LumpSumProgress>(`${this.apiUrl}/${id}/progress`, { 
      headers: this.getAuthHeaders(),
      params: params
    }).pipe(catchError(this.handleError));
  }

  getLumpSumProjection(id: number): Observable<LumpSumProjection[]> {
    return this.http.get<LumpSumProjection[]>(`${this.apiUrl}/${id}/projection`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getTopPerformingInvestments(limit: number = 5): Observable<TopPerformer[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<TopPerformer[]>(`${this.apiUrl}/top-performing`, { 
      headers: this.getAuthHeaders(),
      params: params
    }).pipe(catchError(this.handleError));
  }
}
```

## 7. LumpSum List Component

### lumpsum-list.component.ts
```typescript
import { Component, OnInit } from '@angular/core';
import { LumpSum, LumpSumSummary, TopPerformer } from '../../models/lumpsum.model';
import { LumpSumService } from '../../services/lumpsum.service';

@Component({
  selector: 'app-lump-sum-list',
  templateUrl: './lump-sum-list.component.html',
  styleUrls: ['./lump-sum-list.component.css']
})
export class LumpSumListComponent implements OnInit {
  lumpSums: LumpSum[] = [];
  summary: LumpSumSummary | null = null;
  topPerformers: TopPerformer[] = [];
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(private lumpSumService: LumpSumService) { }

  ngOnInit(): void {
    this.loadLumpSums();
    this.loadSummary();
    this.loadTopPerformers();
  }

  loadLumpSums(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.lumpSumService.getUserLumpSums().subscribe({
      next: (lumpSums) => {
        this.lumpSums = lumpSums;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load lump sum investments. Please try again.';
        this.isLoading = false;
        console.error('Error loading lump sums:', error);
      }
    });
  }

  loadSummary(): void {
    this.lumpSumService.getLumpSumSummary().subscribe({
      next: (summary) => {
        this.summary = summary;
      },
      error: (error) => {
        console.error('Error loading lump sum summary:', error);
      }
    });
  }

  loadTopPerformers(): void {
    this.lumpSumService.getTopPerformingInvestments(5).subscribe({
      next: (performers) => {
        this.topPerformers = performers;
      },
      error: (error) => {
        console.error('Error loading top performers:', error);
      }
    });
  }

  deleteLumpSum(id: number): void {
    if (confirm('Are you sure you want to delete this lump sum investment?')) {
      this.lumpSumService.deleteLumpSum(id).subscribe({
        next: () => {
          this.lumpSums = this.lumpSums.filter(ls => ls.id !== id);
          this.loadSummary();
          this.loadTopPerformers();
        },
        error: (error) => {
          this.errorMessage = 'Failed to delete lump sum investment. Please try again.';
          console.error('Error deleting lump sum:', error);
        }
      });
    }
  }

  calculateReturnPercentage(lumpSum: LumpSum): number {
    if (!lumpSum.principalAmount || !lumpSum.totalInterest) return 0;
    return (lumpSum.totalInterest / lumpSum.principalAmount) * 100;
  }

  getCAGR(lumpSum: LumpSum): number {
    if (!lumpSum.principalAmount || !lumpSum.futureValue || !lumpSum.duration) return 0;
    const cagr = (Math.pow(lumpSum.futureValue / lumpSum.principalAmount, 1 / lumpSum.duration) - 1) * 100;
    return Math.round(cagr * 100) / 100;
  }
}
```

## 8. LumpSum Form Component

### lumpsum-form.component.ts
```typescript
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { LumpSumService } from '../../services/lumpsum.service';
import { LumpSum } from '../../models/lumpsum.model';

@Component({
  selector: 'app-lump-sum-form',
  templateUrl: './lump-sum-form.component.html',
  styleUrls: ['./lump-sum-form.component.css']
})
export class LumpSumFormComponent implements OnInit {
  lumpSumForm: FormGroup;
  isEdit = false;
  lumpSumId?: number;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  calculatedValues: any = {};

  constructor(
    private fb: FormBuilder,
    private lumpSumService: LumpSumService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.lumpSumForm = this.createForm();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEdit = true;
        this.lumpSumId = +params['id'];
        this.loadLumpSum(this.lumpSumId);
      }
    });

    // Recalculate when form values change
    this.lumpSumForm.valueChanges.subscribe(() => {
      this.calculateLumpSum();
    });
  }

  createForm(): FormGroup {
    return this.fb.group({
      investmentName: ['', [Validators.required, Validators.maxLength(100)]],
      principalAmount: [null, [Validators.required, Validators.min(100), Validators.max(100000000)]],
      duration: [null, [Validators.required, Validators.min(1), Validators.max(50)]], // years
      expectedReturn: [null, [Validators.required, Validators.min(1), Validators.max(50)]]
    });
  }

  loadLumpSum(id: number): void {
    this.isLoading = true;
    this.lumpSumService.getLumpSumById(id).subscribe({
      next: (lumpSum) => {
        this.lumpSumForm.patchValue({
          investmentName: lumpSum.investmentName,
          principalAmount: lumpSum.principalAmount,
          duration: lumpSum.duration,
          expectedReturn: lumpSum.expectedReturn
        });
        this.calculatedValues = {
          futureValue: lumpSum.futureValue,
          totalInterest: lumpSum.totalInterest
        };
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load lump sum investment. Please try again.';
        this.isLoading = false;
        console.error('Error loading lump sum:', error);
      }
    });
  }

  calculateLumpSum(): void {
    const formValue = this.lumpSumForm.value;
    if (formValue.principalAmount && formValue.duration && formValue.expectedReturn) {
      const P = formValue.principalAmount;
      const n = formValue.duration;
      const r = formValue.expectedReturn / 100;

      // Compound interest formula: FV = P * (1 + r)^n
      const futureValue = P * Math.pow(1 + r, n);
      const totalInterest = futureValue - P;
      const returnPercentage = (totalInterest / P) * 100;
      const cagr = (Math.pow(futureValue / P, 1 / n) - 1) * 100;

      this.calculatedValues = {
        futureValue: Math.round(futureValue * 100) / 100,
        totalInterest: Math.round(totalInterest * 100) / 100,
        returnPercentage: Math.round(returnPercentage * 100) / 100,
        cagr: Math.round(cagr * 100) / 100
      };
    } else {
      this.calculatedValues = {};
    }
  }

  onSubmit(): void {
    if (this.lumpSumForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const lumpSum: LumpSum = this.lumpSumForm.value;

      const operation = this.isEdit && this.lumpSumId
        ? this.lumpSumService.updateLumpSum(this.lumpSumId, lumpSum)
        : this.lumpSumService.createLumpSum(lumpSum);

      operation.subscribe({
        next: (savedLumpSum) => {
          this.isLoading = false;
          this.successMessage = this.isEdit 
            ? 'Lump sum investment updated successfully!' 
            : 'Lump sum investment created successfully!';
          
          setTimeout(() => {
            this.router.navigate(['/lump-sums']);
          }, 1500);
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = this.isEdit
            ? 'Failed to update lump sum investment. Please try again.'
            : 'Failed to create lump sum investment. Please try again.';
          console.error('Error saving lump sum:', error);
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  markFormGroupTouched(): void {
    Object.keys(this.lumpSumForm.controls).forEach(key => {
      const control = this.lumpSumForm.get(key);
      control?.markAsTouched();
    });
  }

  onCancel(): void {
    if (this.lumpSumForm.dirty) {
      if (confirm('You have unsaved changes. Are you sure you want to leave?')) {
        this.router.navigate(['/lump-sums']);
      }
    } else {
      this.router.navigate(['/lump-sums']);
    }
  }

  // Getters for easy access in template
  get investmentName() { return this.lumpSumForm.get('investmentName'); }
  get principalAmount() { return this.lumpSumForm.get('principalAmount'); }
  get duration() { return this.lumpSumForm.get('duration'); }
  get expectedReturn() { return this.lumpSumForm.get('expectedReturn'); }
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
    LumpSumFormComponent
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

# Create LumpSum investment
curl -X POST http://localhost:8080/api/lump-sums \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "investmentName": "Stock Investment",
    "principalAmount": 100000,
    "duration": 10,
    "expectedReturn": 12
  }'

# Get all lump sum investments
curl -X GET http://localhost:8080/api/lump-sums \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get lump sum summary
curl -X GET http://localhost:8080/api/lump-sums/summary/overview \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get lump sum progress
curl -X GET "http://localhost:8080/api/lump-sums/1/progress?yearsCompleted=5" \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get lump sum projection
curl -X GET http://localhost:8080/api/lump-sums/1/projection \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get top performing investments
curl -X GET "http://localhost:8080/api/lump-sums/top-performing?limit=3" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## Key Features:

1. **Compound Interest Calculations**: Automatic future value and interest calculations
2. **Real-time Updates**: Calculations update as you type
3. **Progress Tracking**: Track investment growth over time
4. **Yearly Projections**: Detailed year-by-year growth projections
5. **Performance Analytics**: CAGR calculations and return percentages
6. **Top Performers**: Identify best-performing investments
7. **Comprehensive Summary**: Overview of all lump sum investments
8. **User-specific Data**: Secure, user-specific investment data

The LumpSum investment management is now fully integrated with your Spring Boot backend and Angular frontend, completing your comprehensive financial management system with Expenses, EMIs, SIPs, and LumpSum investments!