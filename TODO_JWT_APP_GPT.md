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

design the angular application for this todo app which use spring jwt for user authentication and validation and keep track of todo items

# Angular Todo Application with JWT Authentication

I'll design a complete Angular application that integrates with your Spring Boot JWT backend. Here's the implementation:

## 1. Project Structure

```
angular-todo-app/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── login/
│   │   │   ├── register/
│   │   │   ├── todo-list/
│   │   │   ├── todo-item/
│   │   │   └── navbar/
│   │   ├── services/
│   │   ├── guards/
│   │   ├── interceptors/
│   │   ├── models/
│   │   └── utils/
│   ├── assets/
│   └── environments/
```

## 2. Package.json Dependencies

```json
{
  "name": "angular-todo-app",
  "version": "1.0.0",
  "scripts": {
    "ng": "ng",
    "start": "ng serve",
    "build": "ng build",
    "test": "ng test"
  },
  "dependencies": {
    "@angular/animations": "^17.0.0",
    "@angular/common": "^17.0.0",
    "@angular/compiler": "^17.0.0",
    "@angular/core": "^17.0.0",
    "@angular/forms": "^17.0.0",
    "@angular/platform-browser": "^17.0.0",
    "@angular/platform-browser-dynamic": "^17.0.0",
    "@angular/router": "^17.0.0",
    "@angular/service-worker": "^17.0.0",
    "rxjs": "~7.8.0",
    "tslib": "^2.3.0",
    "zone.js": "~0.14.0"
  },
  "devDependencies": {
    "@angular-devkit/build-angular": "^17.0.0",
    "@angular/cli": "^17.0.0",
    "@angular/compiler-cli": "^17.0.0",
    "typescript": "~5.2.0"
  }
}
```

## 3. Environment Configuration

```typescript
// environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

## 4. Data Models

```typescript
// models/user.model.ts
export interface User {
  id?: number;
  username: string;
  email: string;
  roles: string[];
}

// models/auth-request.model.ts
export interface AuthRequest {
  username: string;
  password: string;
}

// models/auth-response.model.ts
export interface AuthResponse {
  token: string;
  type: string;
  username: string;
  email: string;
}

// models/user-registration.model.ts
export interface UserRegistration {
  username: string;
  email: string;
  password: string;
}

// models/todo.model.ts
export interface Todo {
  id?: number;
  title: string;
  description: string;
  completed: boolean;
  createdAt?: string;
  updatedAt?: string;
  user?: User;
}

// models/todo-request.model.ts
export interface TodoRequest {
  title: string;
  description: string;
}
```

## 5. Authentication Service

```typescript
// services/auth.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthRequest, AuthResponse, UserRegistration, User } from '../models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = environment.apiUrl;
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadUserFromStorage();
  }

  private loadUserFromStorage(): void {
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('currentUser');
    
    if (token && userStr) {
      const user: User = JSON.parse(userStr);
      this.currentUserSubject.next(user);
    }
  }

  register(registration: UserRegistration): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/register`, registration);
  }

  login(authRequest: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/auth/login`, authRequest)
      .pipe(
        tap(response => {
          localStorage.setItem('token', response.token);
          const user: User = {
            username: response.username,
            email: response.email,
            roles: ['ROLE_USER'] // Assuming default role
          };
          localStorage.setItem('currentUser', JSON.stringify(user));
          this.currentUserSubject.next(user);
        })
      );
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }
}
```

## 6. Todo Service

```typescript
// services/todo.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Todo, TodoRequest } from '../models';

@Injectable({
  providedIn: 'root'
})
export class TodoService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) { }

  getAllTodos(): Observable<Todo[]> {
    return this.http.get<Todo[]>(`${this.apiUrl}/todos`);
  }

  getCompletedTodos(): Observable<Todo[]> {
    return this.http.get<Todo[]>(`${this.apiUrl}/todos/completed`);
  }

  getPendingTodos(): Observable<Todo[]> {
    return this.http.get<Todo[]>(`${this.apiUrl}/todos/pending`);
  }

  createTodo(todoRequest: TodoRequest): Observable<Todo> {
    return this.http.post<Todo>(`${this.apiUrl}/todos`, todoRequest);
  }

  updateTodo(id: number, todoRequest: TodoRequest): Observable<Todo> {
    return this.http.put<Todo>(`${this.apiUrl}/todos/${id}`, todoRequest);
  }

  toggleTodoCompletion(id: number): Observable<Todo> {
    return this.http.patch<Todo>(`${this.apiUrl}/todos/${id}/toggle`, {});
  }

  deleteTodo(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/todos/${id}`);
  }
}
```

## 7. JWT Interceptor

```typescript
// interceptors/jwt.interceptor.ts
import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(request);
  }
}
```

## 8. Auth Guard

```typescript
// guards/auth.guard.ts
import { Injectable } from '@angular/core';
import { Router, CanActivate } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(): boolean {
    if (this.authService.isAuthenticated()) {
      return true;
    } else {
      this.router.navigate(['/login']);
      return false;
    }
  }
}
```

## 9. Components

### Login Component

```typescript
// components/login/login.component.ts
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { AuthRequest } from '../../models';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm: FormGroup;
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';

      const authRequest: AuthRequest = this.loginForm.value;

      this.authService.login(authRequest).subscribe({
        next: () => {
          this.router.navigate(['/todos']);
        },
        error: (error) => {
          this.errorMessage = error.error?.message || 'Login failed';
          this.isLoading = false;
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    }
  }
}
```

```html
<!-- components/login/login.component.html -->
<div class="login-container">
  <div class="card">
    <div class="card-header">
      <h3>Login</h3>
    </div>
    <div class="card-body">
      <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
        <div class="form-group">
          <label for="username">Username</label>
          <input
            type="text"
            id="username"
            formControlName="username"
            class="form-control"
            [class.is-invalid]="loginForm.get('username')?.invalid && loginForm.get('username')?.touched"
          >
          <div class="invalid-feedback" *ngIf="loginForm.get('username')?.errors?.['required'] && loginForm.get('username')?.touched">
            Username is required
          </div>
        </div>

        <div class="form-group">
          <label for="password">Password</label>
          <input
            type="password"
            id="password"
            formControlName="password"
            class="form-control"
            [class.is-invalid]="loginForm.get('password')?.invalid && loginForm.get('password')?.touched"
          >
          <div class="invalid-feedback" *ngIf="loginForm.get('password')?.errors?.['required'] && loginForm.get('password')?.touched">
            Password is required
          </div>
          <div class="invalid-feedback" *ngIf="loginForm.get('password')?.errors?.['minlength'] && loginForm.get('password')?.touched">
            Password must be at least 6 characters
          </div>
        </div>

        <div class="alert alert-danger" *ngIf="errorMessage">
          {{ errorMessage }}
        </div>

        <button
          type="submit"
          class="btn btn-primary btn-block"
          [disabled]="loginForm.invalid || isLoading"
        >
          <span *ngIf="isLoading" class="spinner-border spinner-border-sm"></span>
          {{ isLoading ? 'Logging in...' : 'Login' }}
        </button>

        <div class="text-center mt-3">
          <a routerLink="/register">Don't have an account? Register</a>
        </div>
      </form>
    </div>
  </div>
</div>
```

### Register Component

```typescript
// components/register/register.component.ts
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { UserRegistration } from '../../models';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  registerForm: FormGroup;
  errorMessage: string = '';
  successMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password');
    const confirmPassword = form.get('confirmPassword');
    
    if (password && confirmPassword && password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
    } else {
      confirmPassword?.setErrors(null);
    }
  }

  onSubmit(): void {
    if (this.registerForm.valid) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      const registration: UserRegistration = {
        username: this.registerForm.value.username,
        email: this.registerForm.value.email,
        password: this.registerForm.value.password
      };

      this.authService.register(registration).subscribe({
        next: () => {
          this.successMessage = 'Registration successful! Please login.';
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        },
        error: (error) => {
          this.errorMessage = error.error?.message || 'Registration failed';
          this.isLoading = false;
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    }
  }
}
```

```html
<!-- components/register/register.component.html -->
<div class="register-container">
  <div class="card">
    <div class="card-header">
      <h3>Register</h3>
    </div>
    <div class="card-body">
      <form [formGroup]="registerForm" (ngSubmit)="onSubmit()">
        <div class="form-group">
          <label for="username">Username</label>
          <input
            type="text"
            id="username"
            formControlName="username"
            class="form-control"
            [class.is-invalid]="registerForm.get('username')?.invalid && registerForm.get('username')?.touched"
          >
          <div class="invalid-feedback" *ngIf="registerForm.get('username')?.errors?.['required'] && registerForm.get('username')?.touched">
            Username is required
          </div>
          <div class="invalid-feedback" *ngIf="registerForm.get('username')?.errors?.['minlength'] && registerForm.get('username')?.touched">
            Username must be at least 3 characters
          </div>
        </div>

        <div class="form-group">
          <label for="email">Email</label>
          <input
            type="email"
            id="email"
            formControlName="email"
            class="form-control"
            [class.is-invalid]="registerForm.get('email')?.invalid && registerForm.get('email')?.touched"
          >
          <div class="invalid-feedback" *ngIf="registerForm.get('email')?.errors?.['required'] && registerForm.get('email')?.touched">
            Email is required
          </div>
          <div class="invalid-feedback" *ngIf="registerForm.get('email')?.errors?.['email'] && registerForm.get('email')?.touched">
            Please enter a valid email
          </div>
        </div>

        <div class="form-group">
          <label for="password">Password</label>
          <input
            type="password"
            id="password"
            formControlName="password"
            class="form-control"
            [class.is-invalid]="registerForm.get('password')?.invalid && registerForm.get('password')?.touched"
          >
          <div class="invalid-feedback" *ngIf="registerForm.get('password')?.errors?.['required'] && registerForm.get('password')?.touched">
            Password is required
          </div>
          <div class="invalid-feedback" *ngIf="registerForm.get('password')?.errors?.['minlength'] && registerForm.get('password')?.touched">
            Password must be at least 6 characters
          </div>
        </div>

        <div class="form-group">
          <label for="confirmPassword">Confirm Password</label>
          <input
            type="password"
            id="confirmPassword"
            formControlName="confirmPassword"
            class="form-control"
            [class.is-invalid]="registerForm.get('confirmPassword')?.invalid && registerForm.get('confirmPassword')?.touched"
          >
          <div class="invalid-feedback" *ngIf="registerForm.get('confirmPassword')?.errors?.['required'] && registerForm.get('confirmPassword')?.touched">
            Please confirm your password
          </div>
          <div class="invalid-feedback" *ngIf="registerForm.get('confirmPassword')?.errors?.['passwordMismatch'] && registerForm.get('confirmPassword')?.touched">
            Passwords do not match
          </div>
        </div>

        <div class="alert alert-danger" *ngIf="errorMessage">
          {{ errorMessage }}
        </div>

        <div class="alert alert-success" *ngIf="successMessage">
          {{ successMessage }}
        </div>

        <button
          type="submit"
          class="btn btn-primary btn-block"
          [disabled]="registerForm.invalid || isLoading"
        >
          <span *ngIf="isLoading" class="spinner-border spinner-border-sm"></span>
          {{ isLoading ? 'Registering...' : 'Register' }}
        </button>

        <div class="text-center mt-3">
          <a routerLink="/login">Already have an account? Login</a>
        </div>
      </form>
    </div>
  </div>
</div>
```

### Todo List Component

```typescript
// components/todo-list/todo-list.component.ts
import { Component, OnInit } from '@angular/core';
import { TodoService } from '../../services/todo.service';
import { AuthService } from '../../services/auth.service';
import { Todo, TodoRequest } from '../../models';

@Component({
  selector: 'app-todo-list',
  templateUrl: './todo-list.component.html',
  styleUrls: ['./todo-list.component.css']
})
export class TodoListComponent implements OnInit {
  todos: Todo[] = [];
  filteredTodos: Todo[] = [];
  filter: 'all' | 'completed' | 'pending' = 'all';
  newTodo: TodoRequest = { title: '', description: '' };
  isLoading: boolean = false;
  errorMessage: string = '';

  constructor(
    private todoService: TodoService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadTodos();
  }

  loadTodos(): void {
    this.isLoading = true;
    this.todoService.getAllTodos().subscribe({
      next: (todos) => {
        this.todos = todos;
        this.applyFilter();
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load todos';
        this.isLoading = false;
      }
    });
  }

  applyFilter(): void {
    switch (this.filter) {
      case 'completed':
        this.filteredTodos = this.todos.filter(todo => todo.completed);
        break;
      case 'pending':
        this.filteredTodos = this.todos.filter(todo => !todo.completed);
        break;
      default:
        this.filteredTodos = this.todos;
    }
  }

  onFilterChange(filter: 'all' | 'completed' | 'pending'): void {
    this.filter = filter;
    this.applyFilter();
  }

  createTodo(): void {
    if (this.newTodo.title.trim()) {
      this.todoService.createTodo(this.newTodo).subscribe({
        next: (todo) => {
          this.todos.unshift(todo);
          this.applyFilter();
          this.newTodo = { title: '', description: '' };
        },
        error: (error) => {
          this.errorMessage = 'Failed to create todo';
        }
      });
    }
  }

  toggleTodoCompletion(todo: Todo): void {
    if (todo.id) {
      this.todoService.toggleTodoCompletion(todo.id).subscribe({
        next: (updatedTodo) => {
          const index = this.todos.findIndex(t => t.id === updatedTodo.id);
          if (index !== -1) {
            this.todos[index] = updatedTodo;
            this.applyFilter();
          }
        },
        error: (error) => {
          this.errorMessage = 'Failed to update todo';
        }
      });
    }
  }

  deleteTodo(todo: Todo): void {
    if (todo.id && confirm('Are you sure you want to delete this todo?')) {
      this.todoService.deleteTodo(todo.id).subscribe({
        next: () => {
          this.todos = this.todos.filter(t => t.id !== todo.id);
          this.applyFilter();
        },
        error: (error) => {
          this.errorMessage = 'Failed to delete todo';
        }
      });
    }
  }

  getCurrentUser() {
    return this.authService.getCurrentUser();
  }

  logout(): void {
    this.authService.logout();
  }
}
```

```html
<!-- components/todo-list/todo-list.component.html -->
<div class="todo-container">
  <div class="header">
    <h2>My Todo List</h2>
    <div class="user-info">
      <span>Welcome, {{ getCurrentUser()?.username }}!</span>
      <button class="btn btn-outline-danger btn-sm" (click)="logout()">Logout</button>
    </div>
  </div>

  <div class="alert alert-danger" *ngIf="errorMessage" (click)="errorMessage = ''">
    {{ errorMessage }}
  </div>

  <!-- Add Todo Form -->
  <div class="add-todo-card card">
    <div class="card-body">
      <h5 class="card-title">Add New Todo</h5>
      <div class="form-group">
        <input
          type="text"
          class="form-control"
          placeholder="Todo title"
          [(ngModel)]="newTodo.title"
          (keyup.enter)="createTodo()"
        >
      </div>
      <div class="form-group">
        <textarea
          class="form-control"
          placeholder="Description (optional)"
          [(ngModel)]="newTodo.description"
          rows="2"
        ></textarea>
      </div>
      <button
        class="btn btn-primary"
        (click)="createTodo()"
        [disabled]="!newTodo.title.trim()"
      >
        Add Todo
      </button>
    </div>
  </div>

  <!-- Filter Buttons -->
  <div class="filter-buttons">
    <button
      class="btn btn-outline-primary"
      [class.active]="filter === 'all'"
      (click)="onFilterChange('all')"
    >
      All ({{ todos.length }})
    </button>
    <button
      class="btn btn-outline-success"
      [class.active]="filter === 'completed'"
      (click)="onFilterChange('completed')"
    >
      Completed ({{ todos.filter(t => t.completed).length }})
    </button>
    <button
      class="btn btn-outline-warning"
      [class.active]="filter === 'pending'"
      (click)="onFilterChange('pending')"
    >
      Pending ({{ todos.filter(t => !t.completed).length }})
    </button>
  </div>

  <!-- Loading Spinner -->
  <div class="text-center" *ngIf="isLoading">
    <div class="spinner-border" role="status">
      <span class="sr-only">Loading...</span>
    </div>
  </div>

  <!-- Todo List -->
  <div class="todo-list" *ngIf="!isLoading">
    <div *ngIf="filteredTodos.length === 0" class="empty-state">
      <p>No todos found.</p>
    </div>

    <app-todo-item
      *ngFor="let todo of filteredTodos"
      [todo]="todo"
      (toggle)="toggleTodoCompletion(todo)"
      (delete)="deleteTodo(todo)"
    ></app-todo-item>
  </div>
</div>
```

### Todo Item Component

```typescript
// components/todo-item/todo-item.component.ts
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Todo } from '../../models';

@Component({
  selector: 'app-todo-item',
  templateUrl: './todo-item.component.html',
  styleUrls: ['./todo-item.component.css']
})
export class TodoItemComponent {
  @Input() todo!: Todo;
  @Output() toggle = new EventEmitter<void>();
  @Output() delete = new EventEmitter<void>();

  onToggle(): void {
    this.toggle.emit();
  }

  onDelete(): void {
    this.delete.emit();
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }
}
```

```html
<!-- components/todo-item/todo-item.component.html -->
<div class="todo-item card" [class.completed]="todo.completed">
  <div class="card-body">
    <div class="todo-content">
      <div class="todo-header">
        <h5 [class.completed]="todo.completed">{{ todo.title }}</h5>
        <div class="todo-actions">
          <button
            class="btn btn-sm"
            [class]="todo.completed ? 'btn-warning' : 'btn-success'"
            (click)="onToggle()"
          >
            {{ todo.completed ? 'Undo' : 'Complete' }}
          </button>
          <button class="btn btn-danger btn-sm" (click)="onDelete()">
            Delete
          </button>
        </div>
      </div>
      
      <p class="todo-description" *ngIf="todo.description">
        {{ todo.description }}
      </p>
      
      <div class="todo-meta">
        <small class="text-muted">
          Created: {{ formatDate(todo.createdAt!) }}
          <span *ngIf="todo.updatedAt && todo.updatedAt !== todo.createdAt">
            | Updated: {{ formatDate(todo.updatedAt) }}
          </span>
        </small>
      </div>
    </div>
  </div>
</div>
```

## 10. App Module Configuration

```typescript
// app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { TodoListComponent } from './components/todo-list/todo-list.component';
import { TodoItemComponent } from './components/todo-item/todo-item.component';

import { JwtInterceptor } from './interceptors/jwt.interceptor';
import { AuthGuard } from './guards/auth.guard';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    TodoListComponent,
    TodoItemComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: JwtInterceptor, multi: true },
    AuthGuard
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
```

## 11. App Routing

```typescript
// app-routing.module.ts
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { TodoListComponent } from './components/todo-list/todo-list.component';
import { AuthGuard } from './guards/auth.guard';

const routes: Routes = [
  { path: '', redirectTo: '/todos', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'todos', component: TodoListComponent, canActivate: [AuthGuard] },
  { path: '**', redirectTo: '/todos' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
```

## 12. Main App Component

```typescript
// app.component.ts
import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  template: `
    <router-outlet></router-outlet>
  `,
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'Todo App';
}
```

## 13. CSS Styles

```css
/* styles.css - Global Styles */
body {
  background-color: #f8f9fa;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.login-container,
.register-container {
  max-width: 400px;
  margin: 100px auto;
  padding: 20px;
}

.todo-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #e9ecef;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.add-todo-card {
  margin-bottom: 30px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.filter-buttons {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.filter-buttons .btn.active {
  background-color: #007bff;
  color: white;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.todo-item {
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.todo-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

.todo-item.completed {
  opacity: 0.7;
  background-color: #f8f9fa;
}

.todo-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.todo-header h5.completed {
  text-decoration: line-through;
  color: #6c757d;
}

.todo-actions {
  display: flex;
  gap: 8px;
}

.todo-description {
  color: #6c757d;
  margin-bottom: 10px;
}

.todo-meta {
  font-size: 0.8rem;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #6c757d;
}

.btn:disabled {
  cursor: not-allowed;
}

/* Responsive Design */
@media (max-width: 768px) {
  .todo-container {
    padding: 10px;
  }
  
  .header {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .todo-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .todo-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
```

## 14. Usage Instructions

1. **Start the Spring Boot backend** on port 8080
2. **Start the Angular development server**:
   ```bash
   ng serve
   ```
3. **Access the application** at `http://localhost:4200`

## 15. Features

- ✅ User registration and login with JWT
- ✅ Automatic JWT token inclusion in API requests
- ✅ Route protection with AuthGuard
- ✅ Create, read, update, delete todos
- ✅ Toggle todo completion status
- ✅ Filter todos by status (all, completed, pending)
- ✅ Responsive design
- ✅ Error handling and loading states
- ✅ User session management

This Angular application provides a complete frontend solution that seamlessly integrates with your Spring Boot JWT backend, offering a smooth user experience for managing todo items with proper authentication and authorization.

Error: src/app/components/todo-list/todo-list.component.html:88:17 - error NG5002: Parser Error: Bindings cannot contain assignments at column 18 in [ Pending ({{ todos.filter(t => !t.completed).length }}) ] in /Users/rs/Project/Backend/todo-ui/todo-angular/src/app/components/todo-list/todo-list.component.html@66:6

88       (delete)="deleteTodo(todo)"
~~~~~~~~~~~~~~~~

src/app/components/todo-list/todo-list.component.ts:10:16
10   templateUrl: './todo-list.component.html',
~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Error occurs in the template of component TodoListComponent.

The error is occurring because Angular templates don't allow method calls with complex logic in template expressions. The issue is with these lines in your template:

```html
Completed ({{ todos.filter(t => t.completed).length }})
Pending ({{ todos.filter(t => !t.completed).length }})
```

Here's the corrected version of the `todo-list.component.ts`:

```typescript
// components/todo-list/todo-list.component.ts
import { Component, OnInit } from '@angular/core';
import { TodoService } from '../../services/todo.service';
import { AuthService } from '../../services/auth.service';
import { Todo, TodoRequest } from '../../models';

@Component({
  selector: 'app-todo-list',
  templateUrl: './todo-list.component.html',
  styleUrls: ['./todo-list.component.css']
})
export class TodoListComponent implements OnInit {
  todos: Todo[] = [];
  filteredTodos: Todo[] = [];
  filter: 'all' | 'completed' | 'pending' = 'all';
  newTodo: TodoRequest = { title: '', description: '' };
  isLoading: boolean = false;
  errorMessage: string = '';

  // Computed properties for counts
  get completedCount(): number {
    return this.todos.filter(t => t.completed).length;
  }

  get pendingCount(): number {
    return this.todos.filter(t => !t.completed).length;
  }

  get allCount(): number {
    return this.todos.length;
  }

  constructor(
    private todoService: TodoService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadTodos();
  }

  loadTodos(): void {
    this.isLoading = true;
    this.todoService.getAllTodos().subscribe({
      next: (todos) => {
        this.todos = todos;
        this.applyFilter();
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load todos';
        this.isLoading = false;
      }
    });
  }

  applyFilter(): void {
    switch (this.filter) {
      case 'completed':
        this.filteredTodos = this.todos.filter(todo => todo.completed);
        break;
      case 'pending':
        this.filteredTodos = this.todos.filter(todo => !todo.completed);
        break;
      default:
        this.filteredTodos = this.todos;
    }
  }

  onFilterChange(filter: 'all' | 'completed' | 'pending'): void {
    this.filter = filter;
    this.applyFilter();
  }

  createTodo(): void {
    if (this.newTodo.title.trim()) {
      this.todoService.createTodo(this.newTodo).subscribe({
        next: (todo) => {
          this.todos.unshift(todo);
          this.applyFilter();
          this.newTodo = { title: '', description: '' };
        },
        error: (error) => {
          this.errorMessage = 'Failed to create todo';
        }
      });
    }
  }

  toggleTodoCompletion(todo: Todo): void {
    if (todo.id) {
      this.todoService.toggleTodoCompletion(todo.id).subscribe({
        next: (updatedTodo) => {
          const index = this.todos.findIndex(t => t.id === updatedTodo.id);
          if (index !== -1) {
            this.todos[index] = updatedTodo;
            this.applyFilter();
          }
        },
        error: (error) => {
          this.errorMessage = 'Failed to update todo';
        }
      });
    }
  }

  deleteTodo(todo: Todo): void {
    if (todo.id && confirm('Are you sure you want to delete this todo?')) {
      this.todoService.deleteTodo(todo.id).subscribe({
        next: () => {
          this.todos = this.todos.filter(t => t.id !== todo.id);
          this.applyFilter();
        },
        error: (error) => {
          this.errorMessage = 'Failed to delete todo';
        }
      });
    }
  }

  getCurrentUser() {
    return this.authService.getCurrentUser();
  }

  logout(): void {
    this.authService.logout();
  }
}
```

And here's the corrected template:

```html
<!-- components/todo-list/todo-list.component.html -->
<div class="todo-container">
  <div class="header">
    <h2>My Todo List</h2>
    <div class="user-info">
      <span>Welcome, {{ getCurrentUser()?.username }}!</span>
      <button class="btn btn-outline-danger btn-sm" (click)="logout()">Logout</button>
    </div>
  </div>

  <div class="alert alert-danger" *ngIf="errorMessage" (click)="errorMessage = ''">
    {{ errorMessage }}
  </div>

  <!-- Add Todo Form -->
  <div class="add-todo-card card">
    <div class="card-body">
      <h5 class="card-title">Add New Todo</h5>
      <div class="form-group">
        <input
          type="text"
          class="form-control"
          placeholder="Todo title"
          [(ngModel)]="newTodo.title"
          (keyup.enter)="createTodo()"
        >
      </div>
      <div class="form-group">
        <textarea
          class="form-control"
          placeholder="Description (optional)"
          [(ngModel)]="newTodo.description"
          rows="2"
        ></textarea>
      </div>
      <button
        class="btn btn-primary"
        (click)="createTodo()"
        [disabled]="!newTodo.title.trim()"
      >
        Add Todo
      </button>
    </div>
  </div>

  <!-- Filter Buttons -->
  <div class="filter-buttons">
    <button
      class="btn btn-outline-primary"
      [class.active]="filter === 'all'"
      (click)="onFilterChange('all')"
    >
      All ({{ allCount }})
    </button>
    <button
      class="btn btn-outline-success"
      [class.active]="filter === 'completed'"
      (click)="onFilterChange('completed')"
    >
      Completed ({{ completedCount }})
    </button>
    <button
      class="btn btn-outline-warning"
      [class.active]="filter === 'pending'"
      (click)="onFilterChange('pending')"
    >
      Pending ({{ pendingCount }})
    </button>
  </div>

  <!-- Loading Spinner -->
  <div class="text-center" *ngIf="isLoading">
    <div class="spinner-border" role="status">
      <span class="sr-only">Loading...</span>
    </div>
  </div>

  <!-- Todo List -->
  <div class="todo-list" *ngIf="!isLoading">
    <div *ngIf="filteredTodos.length === 0" class="empty-state">
      <p>No todos found.</p>
    </div>

    <app-todo-item
      *ngFor="let todo of filteredTodos"
      [todo]="todo"
      (toggle)="toggleTodoCompletion(todo)"
      (delete)="deleteTodo(todo)"
    ></app-todo-item>
  </div>
</div>
```

## Key Changes Made:

1. **Added computed properties** (`completedCount`, `pendingCount`, `allCount`) instead of using filter methods directly in the template
2. **Used getters** to automatically recalculate counts when the todos array changes
3. **Updated template bindings** to use the computed properties instead of method calls

## Why This Fix Works:

- Angular templates don't allow complex expressions with method calls that have arrow functions
- Using getters is more efficient as Angular's change detection will automatically update the counts when the todos array changes
- The template becomes cleaner and more maintainable
- Better separation of concerns - logic stays in the component class, template handles display

The error should now be resolved and the todo counts will display correctly for each filter category.