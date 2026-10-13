package com.teamforge.controller;

import com.teamforge.entity.Account;
import com.teamforge.repository.AccountRepository;
import com.teamforge.service.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // LOGIN FOR ADMIN AND CANDIDATE
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> request
    ) {

        String email = request.get("email");
        String password = request.get("password");

        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message", "Email and password are required."
                    ));
        }

        email = email.trim().toLowerCase();

        Optional<Account> optionalAccount =
                accountRepository.findByEmail(email);

        if (optionalAccount.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message", "Invalid email or password."
                    ));
        }

        Account account = optionalAccount.get();

        if (!passwordEncoder.matches(
                password,
                account.getPassword()
        )) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message", "Invalid email or password."
                    ));
        }

        String token =
                jwtService.generateToken(account.getEmail());

        return ResponseEntity.ok(
                Map.of(
                        "message", "Login successful",
                        "accountId", account.getId(),
                        "name", account.getName(),
                        "email", account.getEmail(),
                        "role", account.getRole().name(),
                        "token", token
                )
        );
    }
}