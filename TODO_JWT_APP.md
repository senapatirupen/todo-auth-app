I'll help you create a Spring Boot application with JWT authentication and a Todo API. Based on the Spring Security concepts from the book, here's a complete implementation:

## 1. Project Structure

```
spring-jwt-todo/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── todoauth/
│       │               ├── config/
│       │               ├── controller/
│       │               ├── dto/
│       │               ├── entity/
│       │               ├── repository/
│       │               ├── security/
│       │               ├── service/
│       │               └── TodoAuthApplication.java
│       └── resources/
│           └── application.properties
```

## 2. Dependencies (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>todo-auth</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.11.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.11.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>
</project>
```

## 3. Application Properties

```properties
# application.properties
server.port=8080

# H2 Database
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true

# JPA
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# JWT
jwt.secret=mySecretKeyWhichShouldBeVeryLongAndSecureInProduction12345
jwt.expiration=86400000
```

## 4. Entity Classes

```java
// User.java
package com.example.todoauth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String username;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles;

    // Constructors, getters, setters
    public User() {}

    public User(String username, String email, String password, List<String> roles) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
```

```java
// Todo.java
package com.example.todoauth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "todos")
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    private String description;
    
    private boolean completed = false;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Constructors, getters, setters
    public Todo() {}

    public Todo(String title, String description, User user) {
        this.title = title;
        this.description = description;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
```

## 5. DTO Classes

```java
// AuthRequest.java
package com.example.todoauth.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthRequest {
    @NotBlank
    private String username;
    
    @NotBlank
    private String password;

    // Constructors, getters, setters
    public AuthRequest() {}

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

```java
// AuthResponse.java
package com.example.todoauth.dto;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String username;
    private String email;

    public AuthResponse(String token, String username, String email) {
        this.token = token;
        this.username = username;
        this.email = email;
    }

    // Getters and setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
```

```java
// UserRegistration.java
package com.example.todoauth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserRegistration {
    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    // Constructors, getters, setters
    public UserRegistration() {}

    public UserRegistration(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

```java
// TodoRequest.java
package com.example.todoauth.dto;

import jakarta.validation.constraints.NotBlank;

public class TodoRequest {
    @NotBlank
    private String title;
    
    private String description;

    // Constructors, getters, setters
    public TodoRequest() {}

    public TodoRequest(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
```

## 6. Repository Interfaces

```java
// UserRepository.java
package com.example.todoauth.repository;

import com.example.todoauth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
```

```java
// TodoRepository.java
package com.example.todoauth.repository;

import com.example.todoauth.entity.Todo;
import com.example.todoauth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findByUser(User user);
    List<Todo> findByUserAndCompleted(User user, boolean completed);
}
```

## 7. JWT Utility Class

```java
// JwtUtil.java
package com.example.todoauth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
```

## 8. Custom UserDetailsService

```java
// CustomUserDetailsService.java
package com.example.todoauth.security;

import com.example.todoauth.entity.User;
import com.example.todoauth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
        );
    }
}
```

## 9. JWT Authentication Filter

```java
// JwtAuthenticationFilter.java
package com.example.todoauth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            
            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

## 10. Security Configuration

```java
// SecurityConfig.java
package com.example.todoauth.config;

import com.example.todoauth.security.CustomUserDetailsService;
import com.example.todoauth.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

## 11. Service Classes

```java
// AuthService.java
package com.example.todoauth.service;

import com.example.todoauth.dto.UserRegistration;
import com.example.todoauth.entity.User;
import com.example.todoauth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Arrays;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(UserRegistration registration) {
        if (userRepository.existsByUsername(registration.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(registration.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(registration.getUsername());
        user.setEmail(registration.getEmail());
        user.setPassword(passwordEncoder.encode(registration.getPassword()));
        user.setRoles(Arrays.asList("ROLE_USER"));

        return userRepository.save(user);
    }
}
```

```java
// TodoService.java
package com.example.todoauth.service;

import com.example.todoauth.dto.TodoRequest;
import com.example.todoauth.entity.Todo;
import com.example.todoauth.entity.User;
import com.example.todoauth.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    public List<Todo> getUserTodos(User user) {
        return todoRepository.findByUser(user);
    }

    public List<Todo> getUserTodosByCompletion(User user, boolean completed) {
        return todoRepository.findByUserAndCompleted(user, completed);
    }

    public Todo createTodo(TodoRequest request, User user) {
        Todo todo = new Todo(request.getTitle(), request.getDescription(), user);
        return todoRepository.save(todo);
    }

    public Optional<Todo> updateTodo(Long id, TodoRequest request, User user) {
        return todoRepository.findById(id)
                .map(todo -> {
                    if (!todo.getUser().getId().equals(user.getId())) {
                        throw new RuntimeException("Not authorized to update this todo");
                    }
                    todo.setTitle(request.getTitle());
                    todo.setDescription(request.getDescription());
                    todo.setUpdatedAt(LocalDateTime.now());
                    return todoRepository.save(todo);
                });
    }

    public Optional<Todo> toggleTodoCompletion(Long id, User user) {
        return todoRepository.findById(id)
                .map(todo -> {
                    if (!todo.getUser().getId().equals(user.getId())) {
                        throw new RuntimeException("Not authorized to update this todo");
                    }
                    todo.setCompleted(!todo.isCompleted());
                    todo.setUpdatedAt(LocalDateTime.now());
                    return todoRepository.save(todo);
                });
    }

    public boolean deleteTodo(Long id, User user) {
        return todoRepository.findById(id)
                .map(todo -> {
                    if (!todo.getUser().getId().equals(user.getId())) {
                        throw new RuntimeException("Not authorized to delete this todo");
                    }
                    todoRepository.delete(todo);
                    return true;
                })
                .orElse(false);
    }
}
```

## 12. Controller Classes

```java
// AuthController.java
package com.example.todoauth.controller;

import com.example.todoauth.dto.AuthRequest;
import com.example.todoauth.dto.AuthResponse;
import com.example.todoauth.dto.UserRegistration;
import com.example.todoauth.entity.User;
import com.example.todoauth.repository.UserRepository;
import com.example.todoauth.security.JwtUtil;
import com.example.todoauth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistration registration) {
        User user = authService.registerUser(registration);
        return ResponseEntity.ok("User registered successfully: " + user.getUsername());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );

        User user = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        final String jwt = jwtUtil.generateToken(
            new org.springframework.security.core.userdetails.User(
                user.getUsername(), 
                user.getPassword(), 
                user.getRoles().stream()
                    .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority(role))
                    .toList()
            )
        );

        return ResponseEntity.ok(new AuthResponse(jwt, user.getUsername(), user.getEmail()));
    }
}
```

```java
// TodoController.java
package com.example.todoauth.controller;

import com.example.todoauth.dto.TodoRequest;
import com.example.todoauth.entity.Todo;
import com.example.todoauth.entity.User;
import com.example.todoauth.repository.UserRepository;
import com.example.todoauth.service.TodoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    @Autowired
    private TodoService todoService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public ResponseEntity<List<Todo>> getUserTodos(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Todo> todos = todoService.getUserTodos(user);
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<Todo>> getCompletedTodos(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Todo> todos = todoService.getUserTodosByCompletion(user, true);
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Todo>> getPendingTodos(Authentication authentication) {
        User user = getCurrentUser(authentication);
        List<Todo> todos = todoService.getUserTodosByCompletion(user, false);
        return ResponseEntity.ok(todos);
    }

    @PostMapping
    public ResponseEntity<Todo> createTodo(@Valid @RequestBody TodoRequest request, 
                                         Authentication authentication) {
        User user = getCurrentUser(authentication);
        Todo todo = todoService.createTodo(request, user);
        return ResponseEntity.ok(todo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(@PathVariable Long id, 
                                         @Valid @RequestBody TodoRequest request,
                                         Authentication authentication) {
        User user = getCurrentUser(authentication);
        Optional<Todo> updatedTodo = todoService.updateTodo(id, request, user);
        return updatedTodo.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Todo> toggleTodoCompletion(@PathVariable Long id,
                                                   Authentication authentication) {
        User user = getCurrentUser(authentication);
        Optional<Todo> updatedTodo = todoService.toggleTodoCompletion(id, user);
        return updatedTodo.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long id, Authentication authentication) {
        User user = getCurrentUser(authentication);
        boolean deleted = todoService.deleteTodo(id, user);
        if (deleted) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
```

## 13. Main Application Class

```java
// TodoAuthApplication.java
package com.example.todoauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TodoAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(TodoAuthApplication.class, args);
    }
}
```

## 14. Testing with cURL Commands

Here are the cURL commands to test the API:

### Register a new user:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "email": "john@example.com",
    "password": "password123"
  }'
```

### Login and get JWT token:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "password": "password123"
  }'
```

### Create a todo (replace TOKEN with actual JWT):
```bash
curl -X POST http://localhost:8080/api/todos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "title": "Learn Spring Security",
    "description": "Study JWT authentication"
  }'
```

### Get all todos:
```bash
curl -X GET http://localhost:8080/api/todos \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

### Update a todo:
```bash
curl -X PUT http://localhost:8080/api/todos/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "title": "Updated Title",
    "description": "Updated description"
  }'
```

### Toggle todo completion:
```bash
curl -X PATCH http://localhost:8080/api/todos/1/toggle \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

### Delete a todo:
```bash
curl -X DELETE http://localhost:8080/api/todos/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

This implementation provides a complete Spring Boot application with JWT authentication and a Todo API, following the Spring Security patterns from the book. The application uses H2 in-memory database for development and includes proper security configurations, password encoding, and JWT token management.

you remember you have design spring-jwt-todo spring application. now add this functionality to monthly expense manage angular and bootstrap application. where all the details are going to store on spring-jwt-todo with mysql db. hope you remeber the model you use is export interface Expense {
category: string;
amount: number;
date: string;
description: string;
inflationRate?: number; // Category-specific inflation rate
}

I'll help you integrate the monthly expense management functionality into your Angular application that connects to your Spring Boot JWT Todo backend. Let me break this down into steps:

## 1. First, let's create the Spring Boot backend entities and endpoints

### Expense Entity (Spring Boot)
```java
@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String category;
    
    @Column(nullable = false)
    private Double amount;
    
    @Column(nullable = false)
    private LocalDate date;
    
    private String description;
    
    private Double inflationRate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    // Constructors, getters, setters
    public Expense() {}
    
    public Expense(String category, Double amount, LocalDate date, String description, Double inflationRate) {
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.inflationRate = inflationRate;
    }
    
    // Getters and setters...
}
```

### Expense Repository
```java
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUser(User user);
    List<Expense> findByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
    List<Expense> findByUserAndCategory(User user, String category);
}
```

### Expense Controller
```java
@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ExpenseController {
    
    @Autowired
    private ExpenseService expenseService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    public ResponseEntity<?> createExpense(@RequestBody Expense expense, Principal principal) {
        try {
            Expense savedExpense = expenseService.createExpense(expense, principal.getName());
            return ResponseEntity.ok(savedExpense);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating expense: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Expense>> getUserExpenses(Principal principal) {
        List<Expense> expenses = expenseService.getUserExpenses(principal.getName());
        return ResponseEntity.ok(expenses);
    }
    
    @GetMapping("/monthly")
    public ResponseEntity<List<Expense>> getMonthlyExpenses(
            @RequestParam int year, 
            @RequestParam int month,
            Principal principal) {
        List<Expense> expenses = expenseService.getMonthlyExpenses(principal.getName(), year, month);
        return ResponseEntity.ok(expenses);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateExpense(@PathVariable Long id, @RequestBody Expense expense) {
        try {
            Expense updatedExpense = expenseService.updateExpense(id, expense);
            return ResponseEntity.ok(updatedExpense);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating expense: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id) {
        try {
            expenseService.deleteExpense(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting expense: " + e.getMessage());
        }
    }
}
```

## 2. Angular Service for Expenses

### expense.service.ts
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Expense } from '../models/expense.model';

@Injectable({
  providedIn: 'root'
})
export class ExpenseService {
  private apiUrl = 'http://localhost:8080/api/expenses';

  constructor(private http: HttpClient) { }

  createExpense(expense: Expense): Observable<Expense> {
    return this.http.post<Expense>(this.apiUrl, expense);
  }

  getUserExpenses(): Observable<Expense[]> {
    return this.http.get<Expense[]>(this.apiUrl);
  }

  getMonthlyExpenses(year: number, month: number): Observable<Expense[]> {
    const params = new HttpParams()
      .set('year', year.toString())
      .set('month', month.toString());
    return this.http.get<Expense[]>(`${this.apiUrl}/monthly`, { params });
  }

  updateExpense(id: number, expense: Expense): Observable<Expense> {
    return this.http.put<Expense>(`${this.apiUrl}/${id}`, expense);
  }

  deleteExpense(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

## 3. Expense Model (Update your existing interface)

### expense.model.ts
```typescript
export interface Expense {
  id?: number;
  category: string;
  amount: number;
  date: string;
  description: string;
  inflationRate?: number;
}
```

## 4. Expense List Component

### expense-list.component.ts
```typescript
import { Component, OnInit } from '@angular/core';
import { Expense } from '../models/expense.model';
import { ExpenseService } from '../services/expense.service';

@Component({
  selector: 'app-expense-list',
  templateUrl: './expense-list.component.html',
  styleUrls: ['./expense-list.component.css']
})
export class ExpenseListComponent implements OnInit {
  expenses: Expense[] = [];
  filteredExpenses: Expense[] = [];
  selectedMonth: Date = new Date();
  totalAmount: number = 0;
  
  // Filter properties
  categoryFilter: string = '';
  dateFilter: string = '';

  constructor(private expenseService: ExpenseService) { }

  ngOnInit(): void {
    this.loadExpenses();
  }

  loadExpenses(): void {
    const year = this.selectedMonth.getFullYear();
    const month = this.selectedMonth.getMonth() + 1;
    
    this.expenseService.getMonthlyExpenses(year, month).subscribe({
      next: (expenses) => {
        this.expenses = expenses;
        this.filteredExpenses = expenses;
        this.calculateTotal();
      },
      error: (error) => console.error('Error loading expenses:', error)
    });
  }

  calculateTotal(): void {
    this.totalAmount = this.filteredExpenses.reduce((sum, expense) => sum + expense.amount, 0);
  }

  applyFilters(): void {
    this.filteredExpenses = this.expenses.filter(expense => {
      const categoryMatch = !this.categoryFilter || 
        expense.category.toLowerCase().includes(this.categoryFilter.toLowerCase());
      const dateMatch = !this.dateFilter || expense.date.includes(this.dateFilter);
      return categoryMatch && dateMatch;
    });
    this.calculateTotal();
  }

  onMonthChange(): void {
    this.loadExpenses();
  }

  deleteExpense(id: number): void {
    if (confirm('Are you sure you want to delete this expense?')) {
      this.expenseService.deleteExpense(id).subscribe({
        next: () => {
          this.expenses = this.expenses.filter(expense => expense.id !== id);
          this.filteredExpenses = this.filteredExpenses.filter(expense => expense.id !== id);
          this.calculateTotal();
        },
        error: (error) => console.error('Error deleting expense:', error)
      });
    }
  }
}
```

### expense-list.component.html
```html
<div class="container mt-4">
  <div class="row">
    <div class="col-12">
      <div class="card">
        <div class="card-header d-flex justify-content-between align-items-center">
          <h4>Monthly Expenses</h4>
          <button class="btn btn-primary" routerLink="/expenses/new">
            <i class="fas fa-plus"></i> Add Expense
          </button>
        </div>
        <div class="card-body">
          <!-- Filters -->
          <div class="row mb-3">
            <div class="col-md-3">
              <label>Select Month</label>
              <input type="month" class="form-control" 
                     [value]="selectedMonth | date:'yyyy-MM'"
                     (change)="selectedMonth = $event.target.valueAsDate; onMonthChange()">
            </div>
            <div class="col-md-3">
              <label>Filter by Category</label>
              <input type="text" class="form-control" 
                     [(ngModel)]="categoryFilter" 
                     (input)="applyFilters()"
                     placeholder="Enter category">
            </div>
            <div class="col-md-3">
              <label>Filter by Date</label>
              <input type="date" class="form-control" 
                     [(ngModel)]="dateFilter" 
                     (change)="applyFilters()">
            </div>
            <div class="col-md-3">
              <label>Total Amount</label>
              <div class="form-control bg-light">
                <strong>${{totalAmount | number:'1.2-2'}}</strong>
              </div>
            </div>
          </div>

          <!-- Expenses Table -->
          <div class="table-responsive">
            <table class="table table-striped table-hover">
              <thead class="thead-dark">
                <tr>
                  <th>Date</th>
                  <th>Category</th>
                  <th>Description</th>
                  <th>Amount</th>
                  <th>Inflation Rate</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let expense of filteredExpenses">
                  <td>{{expense.date | date:'mediumDate'}}</td>
                  <td>
                    <span class="badge badge-primary">{{expense.category}}</span>
                  </td>
                  <td>{{expense.description}}</td>
                  <td>
                    <strong>${{expense.amount | number:'1.2-2'}}</strong>
                  </td>
                  <td>
                    <span *ngIf="expense.inflationRate" 
                          [class]="expense.inflationRate >= 0 ? 'text-success' : 'text-danger'">
                      {{expense.inflationRate | number:'1.2-2'}}%
                    </span>
                    <span *ngIf="!expense.inflationRate" class="text-muted">-</span>
                  </td>
                  <td>
                    <button class="btn btn-sm btn-warning mr-2" 
                            [routerLink]="['/expenses/edit', expense.id]">
                      <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-danger" 
                            (click)="deleteExpense(expense.id!)">
                      <i class="fas fa-trash"></i>
                    </button>
                  </td>
                </tr>
                <tr *ngIf="filteredExpenses.length === 0">
                  <td colspan="6" class="text-center text-muted">
                    No expenses found for the selected period.
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
```

## 5. Expense Form Component

### expense-form.component.ts
```typescript
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ExpenseService } from '../services/expense.service';
import { Expense } from '../models/expense.model';

@Component({
  selector: 'app-expense-form',
  templateUrl: './expense-form.component.html',
  styleUrls: ['./expense-form.component.css']
})
export class ExpenseFormComponent implements OnInit {
  expenseForm: FormGroup;
  isEdit = false;
  expenseId?: number;
  categories = ['Food', 'Transportation', 'Housing', 'Entertainment', 'Healthcare', 'Utilities', 'Other'];

  constructor(
    private fb: FormBuilder,
    private expenseService: ExpenseService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.expenseForm = this.createForm();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEdit = true;
        this.expenseId = +params['id'];
        this.loadExpense(this.expenseId);
      }
    });
  }

  createForm(): FormGroup {
    return this.fb.group({
      category: ['', Validators.required],
      amount: [0, [Validators.required, Validators.min(0.01)]],
      date: [new Date().toISOString().substring(0, 10), Validators.required],
      description: ['', Validators.required],
      inflationRate: [0]
    });
  }

  loadExpense(id: number): void {
    this.expenseService.getUserExpenses().subscribe(expenses => {
      const expense = expenses.find(e => e.id === id);
      if (expense) {
        this.expenseForm.patchValue({
          category: expense.category,
          amount: expense.amount,
          date: expense.date.substring(0, 10),
          description: expense.description,
          inflationRate: expense.inflationRate || 0
        });
      }
    });
  }

  onSubmit(): void {
    if (this.expenseForm.valid) {
      const expense: Expense = this.expenseForm.value;
      
      if (this.isEdit && this.expenseId) {
        this.expenseService.updateExpense(this.expenseId, expense).subscribe({
          next: () => this.router.navigate(['/expenses']),
          error: (error) => console.error('Error updating expense:', error)
        });
      } else {
        this.expenseService.createExpense(expense).subscribe({
          next: () => this.router.navigate(['/expenses']),
          error: (error) => console.error('Error creating expense:', error)
        });
      }
    }
  }
}
```

### expense-form.component.html
```html
<div class="container mt-4">
  <div class="row justify-content-center">
    <div class="col-md-8">
      <div class="card">
        <div class="card-header">
          <h4>{{isEdit ? 'Edit' : 'Add'}} Expense</h4>
        </div>
        <div class="card-body">
          <form [formGroup]="expenseForm" (ngSubmit)="onSubmit()">
            <div class="form-group">
              <label for="category">Category *</label>
              <select class="form-control" id="category" formControlName="category" 
                      [class.is-invalid]="expenseForm.get('category')?.invalid && expenseForm.get('category')?.touched">
                <option value="">Select Category</option>
                <option *ngFor="let category of categories" [value]="category">
                  {{category}}
                </option>
              </select>
              <div class="invalid-feedback" *ngIf="expenseForm.get('category')?.invalid && expenseForm.get('category')?.touched">
                Category is required
              </div>
            </div>

            <div class="form-group">
              <label for="amount">Amount *</label>
              <div class="input-group">
                <div class="input-group-prepend">
                  <span class="input-group-text">$</span>
                </div>
                <input type="number" class="form-control" id="amount" formControlName="amount" 
                       step="0.01" min="0.01"
                       [class.is-invalid]="expenseForm.get('amount')?.invalid && expenseForm.get('amount')?.touched">
              </div>
              <div class="invalid-feedback" *ngIf="expenseForm.get('amount')?.invalid && expenseForm.get('amount')?.touched">
                Valid amount is required (minimum $0.01)
              </div>
            </div>

            <div class="form-group">
              <label for="date">Date *</label>
              <input type="date" class="form-control" id="date" formControlName="date"
                     [class.is-invalid]="expenseForm.get('date')?.invalid && expenseForm.get('date')?.touched">
              <div class="invalid-feedback" *ngIf="expenseForm.get('date')?.invalid && expenseForm.get('date')?.touched">
                Date is required
              </div>
            </div>

            <div class="form-group">
              <label for="description">Description *</label>
              <textarea class="form-control" id="description" formControlName="description" rows="3"
                        [class.is-invalid]="expenseForm.get('description')?.invalid && expenseForm.get('description')?.touched"
                        placeholder="Enter expense description"></textarea>
              <div class="invalid-feedback" *ngIf="expenseForm.get('description')?.invalid && expenseForm.get('description')?.touched">
                Description is required
              </div>
            </div>

            <div class="form-group">
              <label for="inflationRate">Inflation Rate (%)</label>
              <input type="number" class="form-control" id="inflationRate" formControlName="inflationRate" 
                     step="0.01" placeholder="Optional category-specific inflation rate">
            </div>

            <div class="form-group">
              <button type="submit" class="btn btn-primary mr-2" 
                      [disabled]="expenseForm.invalid">
                {{isEdit ? 'Update' : 'Create'}} Expense
              </button>
              <button type="button" class="btn btn-secondary" 
                      (click)="router.navigate(['/expenses'])">
                Cancel
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</div>
```

## 6. Update App Routing Module

### app-routing.module.ts
```typescript
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ExpenseListComponent } from './components/expense-list/expense-list.component';
import { ExpenseFormComponent } from './components/expense-form/expense-form.component';

const routes: Routes = [
  { path: 'expenses', component: ExpenseListComponent },
  { path: 'expenses/new', component: ExpenseFormComponent },
  { path: 'expenses/edit/:id', component: ExpenseFormComponent },
  { path: '', redirectTo: '/expenses', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
```

## 7. Update App Module

### app.module.ts
```typescript
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ExpenseListComponent } from './components/expense-list/expense-list.component';
import { ExpenseFormComponent } from './components/expense-form/expense-form.component';

@NgModule({
  declarations: [
    AppComponent,
    ExpenseListComponent,
    ExpenseFormComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
```

This implementation provides:

1. **Full CRUD operations** for expenses
2. **Monthly filtering** and viewing
3. **Category-based organization**
4. **Inflation rate tracking** per category
5. **Bootstrap-styled responsive UI**
6. **Form validation**
7. **JWT authentication integration** (using your existing setup)

Make sure your Spring Boot backend has CORS configured properly and that the JWT authentication is working with these new endpoints. The expenses will be stored in your MySQL database with proper user association.

Here are the CURL commands to test your Spring Boot Expense API endpoints:

## 1. Authentication First (Get JWT Token)

### Login to get JWT token:
```bash
# Login and get JWT token (replace with your actual credentials)
curl -X POST http://localhost:8080/api/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "username": "your-username",
    "password": "your-password"
  }'
```

Save the token from the response to use in subsequent requests:
```bash
export JWT_TOKEN="your-jwt-token-here"
```

## 2. Expense CRUD Operations

### Create a New Expense:
```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "category": "Food",
    "amount": 75.50,
    "date": "2024-01-15",
    "description": "Groceries for the week",
    "inflationRate": 3.2
  }'
```

### Create Another Expense:
```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "category": "Transportation",
    "amount": 45.00,
    "date": "2024-01-16",
    "description": "Gas for car",
    "inflationRate": 5.1
  }'
```

### Create Expense with Different Category:
```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "category": "Entertainment",
    "amount": 120.00,
    "date": "2024-01-10",
    "description": "Movie tickets and dinner",
    "inflationRate": 2.5
  }'
```

## 3. Read Operations

### Get All User Expenses:
```bash
curl -X GET http://localhost:8080/api/expenses \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### Get Monthly Expenses (January 2024):
```bash
curl -X GET "http://localhost:8080/api/expenses/monthly?year=2024&month=1" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### Get Category-wise Expenses Summary:
```bash
curl -X GET "http://localhost:8080/api/expenses/categories?year=2024&month=1" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### Get Total Monthly Expenses:
```bash
curl -X GET "http://localhost:8080/api/expenses/total/monthly?year=2024&month=1" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json"
```

## 4. Get Specific Expense by ID

First, get an expense ID from the list, then:
```bash
# Replace {id} with actual expense ID from your response
curl -X GET http://localhost:8080/api/expenses/1 \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json"
```

## 5. Update Operations

### Update an Existing Expense:
```bash
# Replace {id} with actual expense ID
curl -X PUT http://localhost:8080/api/expenses/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "category": "Food",
    "amount": 85.00,
    "date": "2024-01-15",
    "description": "Groceries and snacks for the week",
    "inflationRate": 3.5
  }'
```

## 6. Delete Operations

### Delete an Expense:
```bash
# Replace {id} with actual expense ID
curl -X DELETE http://localhost:8080/api/expenses/1 \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json"
```

## 7. Complete Test Script

Here's a complete bash script to test all endpoints:

```bash
#!/bin/bash

# Set your credentials
USERNAME="testuser"
PASSWORD="testpassword"
BASE_URL="http://localhost:8080"

echo "=== Testing Spring Boot Expense API ==="

# 1. Login and get JWT token
echo -e "\n1. Authenticating..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/signin" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

echo "Login Response: $LOGIN_RESPONSE"

# Extract JWT token
JWT_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo "JWT Token: $JWT_TOKEN"

if [ -z "$JWT_TOKEN" ]; then
    echo "Failed to get JWT token. Please check credentials."
    exit 1
fi

# 2. Create expenses
echo -e "\n2. Creating expenses..."

CREATE_RESPONSE1=$(curl -s -X POST "$BASE_URL/api/expenses" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "category": "Food",
    "amount": 75.50,
    "date": "2024-01-15",
    "description": "Groceries for the week",
    "inflationRate": 3.2
  }')
echo "Created Expense 1: $CREATE_RESPONSE1"

CREATE_RESPONSE2=$(curl -s -X POST "$BASE_URL/api/expenses" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "category": "Transportation",
    "amount": 45.00,
    "date": "2024-01-16",
    "description": "Gas for car",
    "inflationRate": 5.1
  }')
echo "Created Expense 2: $CREATE_RESPONSE2"

# 3. Get all expenses
echo -e "\n3. Getting all expenses..."
ALL_EXPENSES=$(curl -s -X GET "$BASE_URL/api/expenses" \
  -H "Authorization: Bearer $JWT_TOKEN")
echo "All Expenses: $ALL_EXPENSES"

# 4. Get monthly expenses
echo -e "\n4. Getting monthly expenses (Jan 2024)..."
MONTHLY_EXPENSES=$(curl -s -X GET "$BASE_URL/api/expenses/monthly?year=2024&month=1" \
  -H "Authorization: Bearer $JWT_TOKEN")
echo "Monthly Expenses: $MONTHLY_EXPENSES"

# 5. Get category summary
echo -e "\n5. Getting category-wise summary..."
CATEGORY_SUMMARY=$(curl -s -X GET "$BASE_URL/api/expenses/categories?year=2024&month=1" \
  -H "Authorization: Bearer $JWT_TOKEN")
echo "Category Summary: $CATEGORY_SUMMARY"

# 6. Get total monthly
echo -e "\n6. Getting total monthly expenses..."
TOTAL_MONTHLY=$(curl -s -X GET "$BASE_URL/api/expenses/total/monthly?year=2024&month=1" \
  -H "Authorization: Bearer $JWT_TOKEN")
echo "Total Monthly: $TOTAL_MONTHLY"

# Extract expense ID for update/delete (assuming first expense has ID 1)
EXPENSE_ID=1

# 7. Update expense
echo -e "\n7. Updating expense ID $EXPENSE_ID..."
UPDATE_RESPONSE=$(curl -s -X PUT "$BASE_URL/api/expenses/$EXPENSE_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "category": "Food",
    "amount": 85.00,
    "date": "2024-01-15",
    "description": "Updated groceries description",
    "inflationRate": 3.5
  }')
echo "Update Response: $UPDATE_RESPONSE"

# 8. Delete expense
echo -e "\n8. Deleting expense ID $EXPENSE_ID..."
DELETE_RESPONSE=$(curl -s -X DELETE "$BASE_URL/api/expenses/$EXPENSE_ID" \
  -H "Authorization: Bearer $JWT_TOKEN")
echo "Delete Response: $DELETE_RESPONSE"

# 9. Verify deletion
echo -e "\n9. Verifying deletion..."
FINAL_EXPENSES=$(curl -s -X GET "$BASE_URL/api/expenses" \
  -H "Authorization: Bearer $JWT_TOKEN")
echo "Final Expenses: $FINAL_EXPENSES"

echo -e "\n=== Testing Complete ==="
```

## 8. Quick One-liner Tests

### Quick Create:
```bash
curl -X POST http://localhost:8080/api/expenses -H "Content-Type: application/json" -H "Authorization: Bearer $JWT_TOKEN" -d '{"category":"Test","amount":100,"date":"2024-01-20","description":"Test expense"}'
```

### Quick Get All:
```bash
curl -X GET http://localhost:8080/api/expenses -H "Authorization: Bearer $JWT_TOKEN"
```

## Important Notes:

1. **Replace credentials** with your actual username/password
2. **Get JWT token first** before testing expense endpoints
3. **Replace {id}** with actual expense IDs from your responses
4. **Adjust dates** if testing in different months/years
5. **Ensure your Spring Boot app** is running on port 8080

## Expected Responses:

- **Success**: 200 OK with data
- **Created**: 200 OK with created expense
- **Not Found**: 404 if expense doesn't exist
- **Unauthorized**: 401 if JWT token is missing/invalid
- **Bad Request**: 400 if validation fails

Run these commands in sequence to thoroughly test your expense management API!

now integrate this detail to spring application. export interface EMI {
id?: number;
emiForName: string;
principal: number | null;
annualInterestRate: number | null;
totalTenure: number | null;
tenuresPaid: number;
emiAmount: number | null;
principalPaidSoFar: number | null;
interestPaidSoFar: number | null;
remainingPrincipal: number | null;
interestToBePaid: number | null;
remainingTenure: number | null;
} like you integrate expense management

I'll integrate the EMI management functionality into your Spring Boot application similar to how we did for expenses. Here's the complete implementation:

## 1. EMI Entity

### EMI.java
```java
@Entity
@Table(name = "emis")
public class EMI {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "emi_for_name", nullable = false)
    private String emiForName;
    
    @Column(name = "principal", nullable = false)
    private Double principal;
    
    @Column(name = "annual_interest_rate", nullable = false)
    private Double annualInterestRate;
    
    @Column(name = "total_tenure", nullable = false)
    private Integer totalTenure;
    
    @Column(name = "tenures_paid", nullable = false)
    private Integer tenuresPaid = 0;
    
    @Column(name = "emi_amount")
    private Double emiAmount;
    
    @Column(name = "principal_paid_so_far")
    private Double principalPaidSoFar = 0.0;
    
    @Column(name = "interest_paid_so_far")
    private Double interestPaidSoFar = 0.0;
    
    @Column(name = "remaining_principal")
    private Double remainingPrincipal;
    
    @Column(name = "interest_to_be_paid")
    private Double interestToBePaid;
    
    @Column(name = "remaining_tenure")
    private Integer remainingTenure;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public EMI() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public EMI(String emiForName, Double principal, Double annualInterestRate, Integer totalTenure) {
        this();
        this.emiForName = emiForName;
        this.principal = principal;
        this.annualInterestRate = annualInterestRate;
        this.totalTenure = totalTenure;
        calculateEMI();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmiForName() {
        return emiForName;
    }
    
    public void setEmiForName(String emiForName) {
        this.emiForName = emiForName;
    }
    
    public Double getPrincipal() {
        return principal;
    }
    
    public void setPrincipal(Double principal) {
        this.principal = principal;
        calculateEMI();
    }
    
    public Double getAnnualInterestRate() {
        return annualInterestRate;
    }
    
    public void setAnnualInterestRate(Double annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
        calculateEMI();
    }
    
    public Integer getTotalTenure() {
        return totalTenure;
    }
    
    public void setTotalTenure(Integer totalTenure) {
        this.totalTenure = totalTenure;
        calculateEMI();
    }
    
    public Integer getTenuresPaid() {
        return tenuresPaid;
    }
    
    public void setTenuresPaid(Integer tenuresPaid) {
        this.tenuresPaid = tenuresPaid;
        calculateRemainingDetails();
    }
    
    public Double getEmiAmount() {
        return emiAmount;
    }
    
    public void setEmiAmount(Double emiAmount) {
        this.emiAmount = emiAmount;
    }
    
    public Double getPrincipalPaidSoFar() {
        return principalPaidSoFar;
    }
    
    public void setPrincipalPaidSoFar(Double principalPaidSoFar) {
        this.principalPaidSoFar = principalPaidSoFar;
    }
    
    public Double getInterestPaidSoFar() {
        return interestPaidSoFar;
    }
    
    public void setInterestPaidSoFar(Double interestPaidSoFar) {
        this.interestPaidSoFar = interestPaidSoFar;
    }
    
    public Double getRemainingPrincipal() {
        return remainingPrincipal;
    }
    
    public void setRemainingPrincipal(Double remainingPrincipal) {
        this.remainingPrincipal = remainingPrincipal;
    }
    
    public Double getInterestToBePaid() {
        return interestToBePaid;
    }
    
    public void setInterestToBePaid(Double interestToBePaid) {
        this.interestToBePaid = interestToBePaid;
    }
    
    public Integer getRemainingTenure() {
        return remainingTenure;
    }
    
    public void setRemainingTenure(Integer remainingTenure) {
        this.remainingTenure = remainingTenure;
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
    
    // EMI Calculation Methods
    public void calculateEMI() {
        if (principal != null && annualInterestRate != null && totalTenure != null) {
            double monthlyInterestRate = annualInterestRate / 12 / 100;
            double emi = principal * monthlyInterestRate * 
                        Math.pow(1 + monthlyInterestRate, totalTenure) / 
                        (Math.pow(1 + monthlyInterestRate, totalTenure) - 1);
            this.emiAmount = Math.round(emi * 100.0) / 100.0;
            calculateRemainingDetails();
        }
    }
    
    public void calculateRemainingDetails() {
        if (principal != null && annualInterestRate != null && totalTenure != null && tenuresPaid != null) {
            double monthlyInterestRate = annualInterestRate / 12 / 100;
            double remainingPrincipal = principal;
            double totalInterestPaid = 0.0;
            double totalPrincipalPaid = 0.0;
            
            // Calculate paid amounts
            for (int i = 0; i < tenuresPaid; i++) {
                double interestComponent = remainingPrincipal * monthlyInterestRate;
                double principalComponent = emiAmount - interestComponent;
                
                totalInterestPaid += interestComponent;
                totalPrincipalPaid += principalComponent;
                remainingPrincipal -= principalComponent;
            }
            
            this.principalPaidSoFar = Math.round(totalPrincipalPaid * 100.0) / 100.0;
            this.interestPaidSoFar = Math.round(totalInterestPaid * 100.0) / 100.0;
            this.remainingPrincipal = Math.round(remainingPrincipal * 100.0) / 100.0;
            this.remainingTenure = totalTenure - tenuresPaid;
            
            // Calculate remaining interest
            double remainingInterest = 0.0;
            double tempPrincipal = remainingPrincipal;
            for (int i = 0; i < remainingTenure; i++) {
                double interestComponent = tempPrincipal * monthlyInterestRate;
                double principalComponent = emiAmount - interestComponent;
                remainingInterest += interestComponent;
                tempPrincipal -= principalComponent;
            }
            
            this.interestToBePaid = Math.round(remainingInterest * 100.0) / 100.0;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    public void makePayment() {
        if (tenuresPaid < totalTenure) {
            this.tenuresPaid++;
            calculateRemainingDetails();
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "EMI{" +
                "id=" + id +
                ", emiForName='" + emiForName + '\'' +
                ", principal=" + principal +
                ", annualInterestRate=" + annualInterestRate +
                ", totalTenure=" + totalTenure +
                ", tenuresPaid=" + tenuresPaid +
                ", emiAmount=" + emiAmount +
                ", remainingPrincipal=" + remainingPrincipal +
                '}';
    }
}
```

## 2. EMI Repository

### EMIRepository.java
```java
@Repository
public interface EMIRepository extends JpaRepository<EMI, Long> {
    List<EMI> findByUser(User user);
    List<EMI> findByUserOrderByCreatedAtDesc(User user);
    Optional<EMI> findByIdAndUser(Long id, User user);
    void deleteByIdAndUser(Long id, User user);
    
    @Query("SELECT SUM(e.emiAmount) FROM EMI e WHERE e.user = :user AND e.remainingTenure > 0")
    Optional<Double> findTotalMonthlyEMIByUser(@Param("user") User user);
}
```

## 3. EMI Service

### EMIService.java
```java
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
```

## 4. EMI Controller

### EMIController.java
```java
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
```

## 5. Angular EMI Model

### emi.model.ts
```typescript
export interface EMI {
  id?: number;
  emiForName: string;
  principal: number | null;
  annualInterestRate: number | null;
  totalTenure: number | null;
  tenuresPaid: number;
  emiAmount: number | null;
  principalPaidSoFar: number | null;
  interestPaidSoFar: number | null;
  remainingPrincipal: number | null;
  interestToBePaid: number | null;
  remainingTenure: number | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface EMISummary {
  totalEMIs: number;
  totalPrincipal: number;
  totalMonthlyEMI: number;
  totalPaid: number;
  remainingPrincipal: number;
}
```

## 6. Angular EMI Service

### emi.service.ts
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { EMI, EMISummary } from '../models/emi.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class EMIService {
  private apiUrl = 'http://localhost:8080/api/emis';

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

  createEMI(emi: EMI): Observable<EMI> {
    return this.http.post<EMI>(this.apiUrl, emi, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getUserEMIs(): Observable<EMI[]> {
    return this.http.get<EMI[]>(this.apiUrl, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getEMIById(id: number): Observable<EMI> {
    return this.http.get<EMI>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  updateEMI(id: number, emi: EMI): Observable<EMI> {
    return this.http.put<EMI>(`${this.apiUrl}/${id}`, emi, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  deleteEMI(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  makePayment(id: number): Observable<EMI> {
    return this.http.post<EMI>(`${this.apiUrl}/${id}/payment`, {}, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getTotalMonthlyEMI(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/summary/total-monthly`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }

  getEMISummary(): Observable<EMISummary> {
    return this.http.get<EMISummary>(`${this.apiUrl}/summary/overview`, { headers: this.getAuthHeaders() })
      .pipe(catchError(this.handleError));
  }
}
```

## 7. EMI List Component

### emi-list.component.ts
```typescript
import { Component, OnInit } from '@angular/core';
import { EMI, EMISummary } from '../../models/emi.model';
import { EMIService } from '../../services/emi.service';

@Component({
  selector: 'app-emi-list',
  templateUrl: './emi-list.component.html',
  styleUrls: ['./emi-list.component.css']
})
export class EMIListComponent implements OnInit {
  emis: EMI[] = [];
  summary: EMISummary | null = null;
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(private emiService: EMIService) { }

  ngOnInit(): void {
    this.loadEMIs();
    this.loadSummary();
  }

  loadEMIs(): void {
    this.isLoading = true;
    this.errorMessage = '';
    
    this.emiService.getUserEMIs().subscribe({
      next: (emis) => {
        this.emis = emis;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load EMIs. Please try again.';
        this.isLoading = false;
        console.error('Error loading EMIs:', error);
      }
    });
  }

  loadSummary(): void {
    this.emiService.getEMISummary().subscribe({
      next: (summary) => {
        this.summary = summary;
      },
      error: (error) => {
        console.error('Error loading EMI summary:', error);
      }
    });
  }

  deleteEMI(id: number): void {
    if (confirm('Are you sure you want to delete this EMI?')) {
      this.emiService.deleteEMI(id).subscribe({
        next: () => {
          this.emis = this.emis.filter(emi => emi.id !== id);
          this.loadSummary();
        },
        error: (error) => {
          this.errorMessage = 'Failed to delete EMI. Please try again.';
          console.error('Error deleting EMI:', error);
        }
      });
    }
  }

  makePayment(id: number): void {
    if (confirm('Mark this month\'s payment as paid?')) {
      this.emiService.makePayment(id).subscribe({
        next: (updatedEMI) => {
          const index = this.emis.findIndex(emi => emi.id === id);
          if (index !== -1) {
            this.emis[index] = updatedEMI;
          }
          this.loadSummary();
        },
        error: (error) => {
          this.errorMessage = 'Failed to process payment. Please try again.';
          console.error('Error processing payment:', error);
        }
      });
    }
  }

  calculateProgress(emi: EMI): number {
    if (!emi.principal || !emi.principalPaidSoFar) return 0;
    return (emi.principalPaidSoFar / emi.principal) * 100;
  }
}
```

## 8. EMI Form Component

### emi-form.component.ts
```typescript
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EMIService } from '../../services/emi.service';
import { EMI } from '../../models/emi.model';

@Component({
  selector: 'app-emi-form',
  templateUrl: './emi-form.component.html',
  styleUrls: ['./emi-form.component.css']
})
export class EMIFormComponent implements OnInit {
  emiForm: FormGroup;
  isEdit = false;
  emiId?: number;
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  calculatedEMI: number | null = null;

  constructor(
    private fb: FormBuilder,
    private emiService: EMIService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.emiForm = this.createForm();
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEdit = true;
        this.emiId = +params['id'];
        this.loadEMI(this.emiId);
      }
    });

    // Recalculate EMI when form values change
    this.emiForm.valueChanges.subscribe(() => {
      this.calculateEMI();
    });
  }

  createForm(): FormGroup {
    return this.fb.group({
      emiForName: ['', [Validators.required, Validators.maxLength(100)]],
      principal: [null, [Validators.required, Validators.min(1), Validators.max(100000000)]],
      annualInterestRate: [null, [Validators.required, Validators.min(0.01), Validators.max(100)]],
      totalTenure: [null, [Validators.required, Validators.min(1), Validators.max(600)]],
      tenuresPaid: [0, [Validators.min(0), Validators.max(600)]]
    });
  }

  loadEMI(id: number): void {
    this.isLoading = true;
    this.emiService.getEMIById(id).subscribe({
      next: (emi) => {
        this.emiForm.patchValue({
          emiForName: emi.emiForName,
          principal: emi.principal,
          annualInterestRate: emi.annualInterestRate,
          totalTenure: emi.totalTenure,
          tenuresPaid: emi.tenuresPaid
        });
        this.calculatedEMI = emi.emiAmount;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load EMI. Please try again.';
        this.isLoading = false;
        console.error('Error loading EMI:', error);
      }
    });
  }

  calculateEMI(): void {
    const formValue = this.emiForm.value;
    if (formValue.principal && formValue.annualInterestRate && formValue.totalTenure) {
      const principal = formValue.principal;
      const monthlyInterestRate = formValue.annualInterestRate / 12 / 100;
      const tenure = formValue.totalTenure;

      const emi = principal * monthlyInterestRate * 
                 Math.pow(1 + monthlyInterestRate, tenure) / 
                 (Math.pow(1 + monthlyInterestRate, tenure) - 1);
      
      this.calculatedEMI = Math.round(emi * 100) / 100;
    } else {
      this.calculatedEMI = null;
    }
  }

  onSubmit(): void {
    if (this.emiForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const emi: EMI = this.emiForm.value;

      const operation = this.isEdit && this.emiId
        ? this.emiService.updateEMI(this.emiId, emi)
        : this.emiService.createEMI(emi);

      operation.subscribe({
        next: (savedEMI) => {
          this.isLoading = false;
          this.successMessage = this.isEdit 
            ? 'EMI updated successfully!' 
            : 'EMI created successfully!';
          
          setTimeout(() => {
            this.router.navigate(['/emis']);
          }, 1500);
        },
        error: (error) => {
          this.isLoading = false;
          this.errorMessage = this.isEdit
            ? 'Failed to update EMI. Please try again.'
            : 'Failed to create EMI. Please try again.';
          console.error('Error saving EMI:', error);
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  markFormGroupTouched(): void {
    Object.keys(this.emiForm.controls).forEach(key => {
      const control = this.emiForm.get(key);
      control?.markAsTouched();
    });
  }

  onCancel(): void {
    if (this.emiForm.dirty) {
      if (confirm('You have unsaved changes. Are you sure you want to leave?')) {
        this.router.navigate(['/emis']);
      }
    } else {
      this.router.navigate(['/emis']);
    }
  }

  // Getters for easy access in template
  get emiForName() { return this.emiForm.get('emiForName'); }
  get principal() { return this.emiForm.get('principal'); }
  get annualInterestRate() { return this.emiForm.get('annualInterestRate'); }
  get totalTenure() { return this.emiForm.get('totalTenure'); }
  get tenuresPaid() { return this.emiForm.get('tenuresPaid'); }
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
    EMIFormComponent
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

# Create EMI
curl -X POST http://localhost:8080/api/emis \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{
    "emiForName": "Home Loan",
    "principal": 500000,
    "annualInterestRate": 8.5,
    "totalTenure": 240,
    "tenuresPaid": 12
  }'

# Get all EMIs
curl -X GET http://localhost:8080/api/emis \
  -H "Authorization: Bearer $JWT_TOKEN"

# Make payment
curl -X POST http://localhost:8080/api/emis/1/payment \
  -H "Authorization: Bearer $JWT_TOKEN"

# Get EMI summary
curl -X GET http://localhost:8080/api/emis/summary/overview \
  -H "Authorization: Bearer $JWT_TOKEN"
```

## Key Features:

1. **Automatic EMI Calculation**: All EMI calculations are done automatically
2. **Payment Tracking**: Track payments and update remaining amounts
3. **User-specific Data**: Each user sees only their own EMIs
4. **Comprehensive Summary**: Get overview of all EMIs
5. **Real-time Calculations**: EMI amounts calculated in real-time
6. **Progress Tracking**: Visual progress of loan repayment

The EMI management is now fully integrated with your Spring Boot backend and Angular frontend, following the same pattern as the expense management system!

