package com.example.todo.auth.controller;

// AuthController.java
import com.example.todo.auth.dto.AuthRequest;
import com.example.todo.auth.dto.AuthResponse;
import com.example.todo.auth.dto.UserRegistration;
import com.example.todo.auth.entity.User;
import com.example.todo.auth.repository.UserRepository;
import com.example.todo.auth.security.JwtUtil;
import com.example.todo.auth.service.AuthService;
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
        User user = userRepository.findByUsername(authRequest.getUsername())
                .orElse(null);
        if (user == null) {
            // if not found by username, try email. This allows login by either username or email.
            user = userRepository.findByEmail(authRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), authRequest.getPassword())
        );
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