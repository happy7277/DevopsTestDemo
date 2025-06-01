package com.example.demo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final String email;
    private final String passwordHash;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final JwtTokens tokens;
    private final Audit audit;

    AuthController(@Value("${demo.admin-email}") String email, @Value("${demo.admin-password}") String password,
                   JwtTokens tokens, Audit audit) {
        this.email = email;
        this.passwordHash = encoder.encode(password);
        this.tokens = tokens;
        this.audit = audit;
    }

    @GetMapping("/health")
    Map<String, String> health() {
        return Map.of("state", "UP");
    }

    @PostMapping("/auth/login")
    LoginResult login(@Valid @RequestBody Credentials credentials) {
        var password = credentials.password();
        if (password.getBytes(StandardCharsets.UTF_8).length > 72 || !encoder.matches(password, passwordHash)
            || !email.equalsIgnoreCase(credentials.email())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        audit.record(email, "LOGIN", null);
        return new LoginResult(tokens.issue(email), "Bearer", JwtTokens.LIFETIME.toSeconds());
    }

    record Credentials(@Email @NotBlank @Size(max = 254) String email,
                       @NotBlank @Size(max = 72) String password) {}
    record LoginResult(String token, String tokenType, long expiresIn) {}
}
