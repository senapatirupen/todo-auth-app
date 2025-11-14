now integrate this details to spring application. export interface SIP {
id?: number;
investmentOnName: string;
monthlyInvestment: number | null;
duration: number | null;
expectedReturn: number | null;
futureValue: number | null;
totalInvestment: number | null;
totalInterestPaid: number | null;
} like you integrate expense management

I'll integrate the SIP (Systematic Investment Plan) management functionality into your Spring Boot application. Here's the complete implementation:

## 1. SIP Entity

### SIP.java
```java
@Entity
@Table(name = "sips")
public class SIP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "investment_on_name", nullable = false)
    private String investmentOnName;
    
    @Column(name = "monthly_investment", nullable = false)
    private Double monthlyInvestment;
    
    @Column(name = "duration", nullable = false)
    private Integer duration; // in months
    
    @Column(name = "expected_return", nullable = false)
    private Double expectedReturn; // annual expected return percentage
    
    @Column(name = "future_value")
    private Double futureValue;
    
    @Column(name = "total_investment")
    private Double totalInvestment;
    
    @Column(name = "total_interest_paid")
    private Double totalInterestPaid;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public SIP() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public SIP(String investmentOnName, Double monthlyInvestment, Integer duration, Double expectedReturn) {
        this();
        this.investmentOnName = investmentOnName;
        this.monthlyInvestment = monthlyInvestment;
        this.duration = duration;
        this.expectedReturn = expectedReturn;
        calculateSIP();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getInvestmentOnName() {
        return investmentOnName;
    }
    
    public void setInvestmentOnName(String investmentOnName) {
        this.investmentOnName = investmentOnName;
    }
    
    public Double getMonthlyInvestment() {
        return monthlyInvestment;
    }
    
    public void setMonthlyInvestment(Double monthlyInvestment) {
        this.monthlyInvestment = monthlyInvestment;
        calculateSIP();
    }
    
    public Integer getDuration() {
        return duration;
    }
    
    public void setDuration(Integer duration) {
        this.duration = duration;
        calculateSIP();
    }
    
    public Double getExpectedReturn() {
        return expectedReturn;
    }
    
    public void setExpectedReturn(Double expectedReturn) {
        this.expectedReturn = expectedReturn;
        calculateSIP();
    }
    
    public Double getFutureValue() {
        return futureValue;
    }
    
    public void setFutureValue(Double futureValue) {
        this.futureValue = futureValue;
    }
    
    public Double getTotalInvestment() {
        return totalInvestment;
    }
    
    public void setTotalInvestment(Double totalInvestment) {
        this.totalInvestment = totalInvestment;
    }
    
    public Double getTotalInterestPaid() {
        return totalInterestPaid;
    }
    
    public void setTotalInterestPaid(Double totalInterestPaid) {
        this.totalInterestPaid = totalInterestPaid;
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
    
    // SIP Calculation Methods
    public void calculateSIP() {
        if (monthlyInvestment != null && duration != null && expectedReturn != null) {
            // Calculate future value using SIP formula: FV = P * [((1 + r)^n - 1) / r] * (1 + r)
            // where P = monthly investment, r = monthly rate, n = number of months
            double monthlyRate = expectedReturn / 12 / 100;
            double months = duration.doubleValue();
            
            // Future value formula for SIP
            double fv = monthlyInvestment * 
                       (Math.pow(1 + monthlyRate, months) - 1) / monthlyRate * 
                       (1 + monthlyRate);
            
            this.futureValue = Math.round(fv * 100.0) / 100.0;
            this.totalInvestment = Math.round(monthlyInvestment * months * 100.0) / 100.0;
            this.totalInterestPaid = Math.round((futureValue - totalInvestment) * 100.0) / 100.0;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    // Calculate values for a specific period (for progress tracking)
    public Map<String, Double> calculateForPeriod(int monthsCompleted) {
        if (monthlyInvestment != null && expectedReturn != null && monthsCompleted <= duration) {
            double monthlyRate = expectedReturn / 12 / 100;
            
            // Current value after n months
            double currentValue = monthlyInvestment * 
                                (Math.pow(1 + monthlyRate, monthsCompleted) - 1) / monthlyRate * 
                                (1 + monthlyRate);
            
            double investedSoFar = monthlyInvestment * monthsCompleted;
            double interestSoFar = currentValue - investedSoFar;
            
            Map<String, Double> result = new HashMap<>();
            result.put("currentValue", Math.round(currentValue * 100.0) / 100.0);
            result.put("investedSoFar", Math.round(investedSoFar * 100.0) / 100.0);
            result.put("interestSoFar", Math.round(interestSoFar * 100.0) / 100.0);
            result.put("remainingMonths", (double) (duration - monthsCompleted));
            
            return result;
        }
        return Collections.emptyMap();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "SIP{" +
                "id=" + id +
                ", investmentOnName='" + investmentOnName + '\'' +
                ", monthlyInvestment=" + monthlyInvestment +
                ", duration=" + duration +
                ", expectedReturn=" + expectedReturn +
                ", futureValue=" + futureValue +
                ", totalInvestment=" + totalInvestment +
                ", totalInterestPaid=" + totalInterestPaid +
                '}';
    }
}
```

## 2. SIP Repository

### SIPRepository.java
```java
@Repository
public interface SIPRepository extends JpaRepository<SIP, Long> {
    List<SIP> findByUser(User user);
    List<SIP> findByUserOrderByCreatedAtDesc(User user);
    Optional<SIP> findByIdAndUser(Long id, User user);
    void deleteByIdAndUser(Long id, User user);
    
    @Query("SELECT SUM(s.monthlyInvestment) FROM SIP s WHERE s.user = :user")
    Optional<Double> findTotalMonthlyInvestmentByUser(@Param("user") User user);
    
    @Query("SELECT SUM(s.futureValue) FROM SIP s WHERE s.user = :user")
    Optional<Double> findTotalFutureValueByUser(@Param("user") User user);
    
    @Query("SELECT SUM(s.totalInvestment) FROM SIP s WHERE s.user = :user")
    Optional<Double> findTotalInvestmentByUser(@Param("user") User user);
}
```

## 3. SIP Service

### SIPService.java
```java
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
        return sip.calculateForPeriod(monthsCompleted);
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
```

## 4. SIP Controller

### SIPController.java
```java
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
```

## 5. Angular SIP Model

### sip.model.ts
```typescript
export interface SIP {
  id?: number;
  investmentOnName: string;
  monthlyInvestment: number | null;
  duration: number | null;
  expectedReturn: number | null;
  futureValue: number | null;
  totalInvestment: number | null;
  totalInterestPaid: number | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface SIPSummary {
  totalSIPs: number;
  totalMonthlyInvestment: number;
  totalFutureValue: number;
  totalInvestment: number;
  totalExpectedInterest: number;
  averageReturn: number;
}

export interface SIPProgress {
  currentValue: number;
  investedSoFar: number;
  interestSoFar: number;
  remainingMonths: number;
}

export interface SIPProjection {
  month: number;
  monthlyInvestment: number;
  cumulativeInvestment: number;
  interestEarned: number;
  totalValue: number;
}
```

## 6. Angular SIP Service

### sip.service.ts
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { SIP, SIPSummary, SIPProgress, SIPProjection } from '../models/sip.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class SIPService {
  private apiUrl = 'http://localhost:8080/api/sips';

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

  createSIP(sip: SIP): Observable<SIP> {
    return this.http.post<SIP>(this.apiUrl, sip, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getUserSIPs(): Observable<SIP[]> {
    return this.http.get<SIP[]>(this.apiUrl, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getSIPById(id: number): Observable<SIP> {
    return this.http.get<SIP>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  updateSIP(id: number, sip: SIP): Observable<SIP> {
    return this.http.put<SIP>(`${this.apiUrl}/${id}`, sip, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  deleteSIP(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getTotalMonthlyInvestment(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/summary/total-monthly`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getSIPSummary(): Observable<SIPSummary> {
    return this.http.get<SIPSummary>(`${this.apiUrl}/summary/overview`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getSIPProgress(id: number, monthsCompleted: number): Observable<SIPProgress> {
    const params = new HttpParams().set('monthsCompleted', monthsCompleted.toString());
    return this.http.get<SIPProgress>(`${this.apiUrl}/${id}/progress`, { 
      headers: this.getAuthHeaders(),
      params: params
    }).pipe(catchError(this.handleError));
  }

  getSIPProjection(id: number): Observable<SIPProjection[]> {
    return this.http.get<SIPProjection[]>(`${this.apiUrl}/${id}/projection`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }
}
```

## 7. SIP List Component

### sip-list.component.ts
```typescript
import { Component, OnInit } from '@angular/core';
import { SIP, SIPSummary } from '../../models/sip.model';
import { SIPService } from '../../services/sip.service';

@Component({
  selector: 'app-sip-list',
  templateUrl: './sip-list.component.html',
  styleUrls: ['./sip-list.component.css']
})
export class SIPListComponent implements OnInit {
  sips: SIP[] = [];
  summary: SIPSummary | null = null;
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(private sipService: SIPService) { }

  ngOnInit(): void {
    this.loadSIPs();
    this.loadSummary();
  }

  loadSIPs(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.sipService.getUserSIPs().subscribe({
      next: (sips) => {
        this.sips = sips;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load SIPs. Please try again.';
        this.isLoading = false;
        console.error('Error loading SIPs:', error);
      }
    });
  }

  loadSummary(): void {
    this.sipService.getSIPSummary().subscribe({
      next: (summary) => {
        this.summary = summary;
      },
      error: (error) => {
        console.error('Error loading SIP summary:', error);
      }
    });
  }

  deleteSIP(id: number): void {
    if (confirm('Are you sure you want to delete this SIP?')) {
      this.sipService.deleteSIP(id).subscribe({
        next: () => {
          this.sips = this.sips.filter(sip => sip.id !== id);
          this.loadSummary();
        },
        error: (error) => {
          this.errorMessage = 'Failed to delete SIP. Please try again.';
          console.error('Error deleting SIP:', error);
        }
      });
    }
  }

  calculateReturnPercentage(sip: SIP): number {
    if (!sip.totalInvestment || !sip.futureValue) return 0;
    return ((sip.futureValue - sip.totalInvestment) / sip.totalInvestment) * 100;
  }

  getDurationYears(duration: number | null): number {
    return duration ? Math.round(duration / 12) : 0;
  }
}
```

## 8. SIP Form Component

### sip-form.component.ts
```typescript
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { SIPService } from '../../services/sip.service';
import { SIP } from '../../models/sip.model';

@Component({
  selector: 'app-sip-form',
  templateUrl: './sip-form.component.html',
  styleUrls: ['./sip-form.component.css']
})
export class SIPFormComponent implements OnInit {
  sipForm: FormGroup;
  isEdit = false;
  sipId?: number;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  calculatedValues: any = {};

  constructor(
    private fb: FormBuilder,
    private sipService: SIPService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.sipForm = this.createForm();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEdit = true;
        this.sipId = +params['id'];
        this.loadSIP(this.sipId);
      }
    });

    // Recalculate when form values change
    this.sipForm.valueChanges.subscribe(() => {
      this.calculateSIP();
    });
  }

  createForm(): FormGroup {
    return this.fb.group({
      investmentOnName: ['', [Validators.required, Validators.maxLength(100)]],
      monthlyInvestment: [null, [Validators.required, Validators.min(100), Validators.max(1000000)]],
      duration: [null, [Validators.required, Validators.min(1), Validators.max(600)]], // months
      expectedReturn: [null, [Validators.required, Validators.min(1), Validators.max(50)]]
    });
  }

  loadSIP(id: number): void {
    this.isLoading = true;
    this.sipService.getSIPById(id).subscribe({
      next: (sip) => {
        this.sipForm.patchValue({
          investmentOnName: sip.investmentOnName,
          monthlyInvestment: sip.monthlyInvestment,
          duration: sip.duration,
          expectedReturn: sip.expectedReturn
        });
        this.calculatedValues = {
          futureValue: sip.futureValue,
          totalInvestment: sip.totalInvestment,
          totalInterestPaid: sip.totalInterestPaid
        };
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load SIP. Please try again.';
        this.isLoading = false;
        console.error('Error loading SIP:', error);
      }
    });
  }

  calculateSIP(): void {
    const formValue = this.sipForm.value;
    if (formValue.monthlyInvestment && formValue.duration && formValue.expectedReturn) {
      const P = formValue.monthlyInvestment;
      const n = formValue.duration;
      const r = formValue.expectedReturn / 12 / 100;

      // SIP Future Value formula: FV = P * [((1 + r)^n - 1) / r] * (1 + r)
      const futureValue = P * ((Math.pow(1 + r, n) - 1) / r) * (1 + r);
      const totalInvestment = P * n;
      const totalInterestPaid = futureValue - totalInvestment;

      this.calculatedValues = {
        futureValue: Math.round(futureValue * 100) / 100,
        totalInvestment: Math.round(totalInvestment * 100) / 100,
        totalInterestPaid: Math.round(totalInterestPaid * 100) / 100,
        returnPercentage: Math.round((totalInterestPaid / totalInvestment) * 100 * 100) / 100
      };
    } else {
      this.calculatedValues = {};
    }
  }

  onSubmit(): void {
    if (this.sipForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const sip: SIP = this.sipForm.value;

      const operation = this.isEdit && this.sipId
        ? this.sipService.updateSIP(this.sipId, sip)
        : this.sipService.createSIP(sip);

      operation.subscribe({
        next: (savedSIP) => {
          this.isLoading = false;
          this.successMessage = this.isEdit 
            ? 'SIP updated successfully!' 
            : 'SIP created successfully!';
          
          setTimeout(() => {
            this.router.navigate(['/sips']);
          }, 1500);
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = this.isEdit
            ? 'Failed to update SIP. Please try again.'
            : 'Failed to create SIP. Please try again.';
          console.error('Error saving SIP:', error);
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  markFormGroupTouched(): void {
    Object.keys(this.sipForm.controls).forEach(key => {
      const control = this.sipForm.get(key);
      control?.markAsTouched();
    });
  }

  onCancel(): void {
    if (this.sipForm.dirty) {
      if (confirm('You have unsaved changes. Are you sure you want to leave?')) {
        this.router.navigate(['/sips']);
      }
    } else {
      this.router.navigate(['/sips']);
    }
  }

  // Getters for easy access in template
  get investmentOnName() { return this.sipForm.get('investmentOnName'); }
  get monthlyInvestment() { return this.sipForm.get('monthlyInvestment'); }
  get duration() { return this.sipForm.get('duration'); }
  get expectedReturn() { return this.sipForm.get('expectedReturn'); }
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
    SIPFormComponent
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

# Create SIP
curl -X POST http://localhost:8080/api/sips \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "investmentOnName": "Mutual Fund",
    "monthlyInvestment": 5000,
    "duration": 120,
    "expectedReturn": 12
  }'

# Get all SIPs
curl -X GET http://localhost:8080/api/sips \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get SIP summary
curl -X GET http://localhost:8080/api/sips/summary/overview \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get SIP progress
curl -X GET "http://localhost:8080/api/sips/1/progress?monthsCompleted=24" \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get SIP projection
curl -X GET http://localhost:8080/api/sips/1/projection \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## Key Features:

1. **Automatic SIP Calculations**: Future value, total investment, and interest calculations
2. **Real-time Updates**: Calculations update as you type
3. **Progress Tracking**: Track SIP progress over time
4. **Projection Charts**: Monthly projection of investment growth
5. **Comprehensive Summary**: Overview of all SIP investments
6. **User-specific Data**: Each user sees only their own SIPs
7. **Investment Analytics**: Return percentages and performance metrics

The SIP management is now fully integrated with your Spring Boot backend and Angular frontend, following the same secure pattern as expense and EMI management!