package com.rs.gridservice.controller;

import com.rs.gridservice.dto.LoginRequest;
import com.rs.gridservice.dto.LogoutRequest;
import com.rs.gridservice.dto.RefreshRequest;
import com.rs.gridservice.dto.TokenResponse;
import com.rs.gridservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginReturnsTokenFromService() {
        LoginRequest request = new LoginRequest();
        request.setUsername("recepsivri");
        request.setPassword("Passw0rd!");
        TokenResponse tokenResponse = TokenResponse.builder().accessToken("access-token").build();
        when(authService.login(request)).thenReturn(tokenResponse);

        ResponseEntity<TokenResponse> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(tokenResponse);
    }

    @Test
    void refreshReturnsTokenFromService() {
        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("refresh-token");
        TokenResponse tokenResponse = TokenResponse.builder().accessToken("new-access-token").build();
        when(authService.refresh(request)).thenReturn(tokenResponse);

        ResponseEntity<TokenResponse> response = authController.refresh(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(tokenResponse);
    }

    @Test
    void logoutReturnsNoContentAndDelegatesToService() {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-token");

        ResponseEntity<Void> response = authController.logout(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(authService).logout(request);
    }
}
