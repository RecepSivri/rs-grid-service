package com.rs.gridservice.controller;

import com.rs.gridservice.dto.LoginRequest;
import com.rs.gridservice.dto.LogoutRequest;
import com.rs.gridservice.dto.RefreshRequest;
import com.rs.gridservice.dto.TokenResponse;
import com.rs.gridservice.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Kullanicinin kendi kullanici adi/sifresi ile Keycloak'tan token almasini saglayan uc nokta.
 * Kullanici olusturma (signup) zaten UserController#addUser altinda mevcut.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Kullanici adi/sifre ile giris yap, JWT (+ refresh token) don. */
    @PostMapping("/login")
    @SecurityRequirements
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** Refresh token ile yeni bir access token (+ genellikle yeni bir refresh token) al. */
    @PostMapping("/refresh")
    @SecurityRequirements
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    /** Refresh token'i gecersiz kilarak oturumu (session) sonlandir. */
    @PostMapping("/logout")
    @SecurityRequirements
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }
}
