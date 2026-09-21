package com.rs.gridservice.service;

import com.rs.gridservice.dto.LoginRequest;
import com.rs.gridservice.dto.LogoutRequest;
import com.rs.gridservice.dto.RefreshRequest;
import com.rs.gridservice.dto.TokenResponse;

/**
 * Keycloak Admin API'sini degil, Keycloak'in kendi token/logout endpoint'lerini
 * (Resource Owner Password Credentials grant) wrap eden login/logout/refresh servisi.
 */
public interface AuthService {

    TokenResponse login(LoginRequest request);

    TokenResponse refresh(RefreshRequest request);

    void logout(LogoutRequest request);
}
