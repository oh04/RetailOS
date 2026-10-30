package com.retailos.backend;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.core.Authentication;

@RestController
public class AuthController {

    private final RegistrationService service;
    private final AccountService accountService;

    public AuthController(RegistrationService service, AccountService accountService) {
        this.service = service;
        this.accountService = accountService;
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must be at most 72 UTF-8 bytes");
        }
        try {
            UserResponse user = service.register(request.email(), request.password());
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email was already registered");
        }
    }

    @GetMapping("/api/auth/csrf")
    public ResponseEntity<CsrfToken> csrf(CsrfToken token) {
        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .body(token);
    }

    @GetMapping("/api/auth/me")
    public UserResponse me(Authentication authentication) {
        Optional<UserResponse> response = accountService.findByEmail(authentication.getName());
        if (response.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found");
        }
        return response.get();
    }
}