package com.rs.gridservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rs.gridservice.config.KeycloakProperties;
import com.rs.gridservice.dto.LoginRequest;
import com.rs.gridservice.dto.LogoutRequest;
import com.rs.gridservice.dto.RefreshRequest;
import com.rs.gridservice.dto.TokenResponse;
import com.rs.gridservice.exception.InvalidCredentialsException;
import com.rs.gridservice.exception.KeycloakOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakAuthServiceImplTest {

    @Mock
    private RestClient restClient;
    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private RestClient.RequestBodySpec requestBodySpec;
    @Mock
    private RestClient.ResponseSpec responseSpec;

    private KeycloakAuthServiceImpl service;

    @BeforeEach
    void setUp() {
        KeycloakProperties properties = new KeycloakProperties();
        properties.setServerUrl("http://localhost:8080");
        properties.setRealm("user-service");
        properties.setClientId("rs-grid-service");
        properties.setClientSecret("secret");

        service = new KeycloakAuthServiceImpl(properties, new ObjectMapper(), restClient);

        lenient().when(restClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.body(any(MultiValueMap.class))).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    }

    private HttpClientErrorException clientError(HttpStatus status, String body) {
        return HttpClientErrorException.create(status, status.getReasonPhrase(), HttpHeaders.EMPTY,
                body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    }

    private LoginRequest loginRequest() {
        LoginRequest request = new LoginRequest();
        request.setUsername("recepsivri");
        request.setPassword("Rs78349401?");
        return request;
    }

    @Test
    void loginSuccessReturnsTokenResponse() {
        TokenResponse tokenResponse = TokenResponse.builder().accessToken("access-token").build();
        when(responseSpec.body(TokenResponse.class)).thenReturn(tokenResponse);

        TokenResponse result = service.login(loginRequest());

        assertThat(result.getAccessToken()).isEqualTo("access-token");
    }

    @Test
    void loginInvalidGrantWithDescriptionThrowsInvalidCredentialsWithKeycloakMessage() {
        when(responseSpec.body(TokenResponse.class)).thenThrow(
                clientError(HttpStatus.UNAUTHORIZED, "{\"error\":\"invalid_grant\",\"error_description\":\"Invalid user credentials\"}"));

        assertThatThrownBy(() -> service.login(loginRequest()))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid user credentials");
    }

    @Test
    void loginInvalidGrantWithoutDescriptionFallsBackToDefaultMessage() {
        when(responseSpec.body(TokenResponse.class)).thenThrow(
                clientError(HttpStatus.UNAUTHORIZED, "{\"error\":\"invalid_grant\"}"));

        assertThatThrownBy(() -> service.login(loginRequest()))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Kullanici adi veya sifre hatali");
    }

    @Test
    void loginNonInvalidGrantErrorThrowsKeycloakOperationException() {
        when(responseSpec.body(TokenResponse.class)).thenThrow(
                clientError(HttpStatus.BAD_REQUEST, "{\"error\":\"unauthorized_client\",\"error_description\":\"Client not allowed\"}"));

        assertThatThrownBy(() -> service.login(loginRequest()))
                .isInstanceOf(KeycloakOperationException.class)
                .hasMessageContaining("unauthorized_client");
    }

    @Test
    void loginWithUnparseableErrorBodyUsesRawBodyAsDescription() {
        when(responseSpec.body(TokenResponse.class)).thenThrow(
                clientError(HttpStatus.BAD_REQUEST, "not-json"));

        assertThatThrownBy(() -> service.login(loginRequest()))
                .isInstanceOf(KeycloakOperationException.class)
                .hasMessageContaining("not-json");
    }

    @Test
    void loginServerErrorThrowsKeycloakOperationException() {
        when(responseSpec.body(TokenResponse.class))
                .thenThrow(HttpServerErrorException.create(HttpStatus.BAD_GATEWAY, "Bad Gateway", HttpHeaders.EMPTY, new byte[0], null));

        assertThatThrownBy(() -> service.login(loginRequest()))
                .isInstanceOf(KeycloakOperationException.class);
    }

    @Test
    void loginConnectionFailureThrowsKeycloakOperationException() {
        when(responseSpec.body(TokenResponse.class)).thenThrow(new ResourceAccessException("baglanti kurulamadi"));

        assertThatThrownBy(() -> service.login(loginRequest()))
                .isInstanceOf(KeycloakOperationException.class);
    }

    @Test
    void refreshSuccessReturnsTokenResponse() {
        TokenResponse tokenResponse = TokenResponse.builder().accessToken("new-access-token").build();
        when(responseSpec.body(TokenResponse.class)).thenReturn(tokenResponse);

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("refresh-token");

        TokenResponse result = service.refresh(request);

        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
    }

    @Test
    void refreshInvalidGrantWithDescriptionThrowsInvalidCredentialsWithKeycloakMessage() {
        when(responseSpec.body(TokenResponse.class)).thenThrow(
                clientError(HttpStatus.BAD_REQUEST, "{\"error\":\"invalid_grant\",\"error_description\":\"Token is not active\"}"));

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("expired-token");

        assertThatThrownBy(() -> service.refresh(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Token is not active");
    }

    @Test
    void refreshInvalidGrantWithoutDescriptionFallsBackToDefaultMessage() {
        when(responseSpec.body(TokenResponse.class)).thenThrow(
                clientError(HttpStatus.BAD_REQUEST, "{\"error\":\"invalid_grant\"}"));

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("expired-token");

        assertThatThrownBy(() -> service.refresh(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Refresh token gecersiz veya suresi dolmus");
    }

    @Test
    void refreshNonInvalidGrantErrorThrowsKeycloakOperationException() {
        when(responseSpec.body(TokenResponse.class)).thenThrow(
                clientError(HttpStatus.BAD_REQUEST, "{\"error\":\"invalid_client\",\"error_description\":\"bad secret\"}"));

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("refresh-token");

        assertThatThrownBy(() -> service.refresh(request))
                .isInstanceOf(KeycloakOperationException.class)
                .hasMessageContaining("invalid_client");
    }

    @Test
    void refreshServerErrorThrowsKeycloakOperationException() {
        when(responseSpec.body(TokenResponse.class))
                .thenThrow(HttpServerErrorException.create(HttpStatus.BAD_GATEWAY, "Bad Gateway", HttpHeaders.EMPTY, new byte[0], null));

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("refresh-token");

        assertThatThrownBy(() -> service.refresh(request))
                .isInstanceOf(KeycloakOperationException.class);
    }

    @Test
    void refreshConnectionFailureThrowsKeycloakOperationException() {
        when(responseSpec.body(TokenResponse.class)).thenThrow(new ResourceAccessException("baglanti kurulamadi"));

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("refresh-token");

        assertThatThrownBy(() -> service.refresh(request))
                .isInstanceOf(KeycloakOperationException.class);
    }

    @Test
    void logoutSuccessCompletesWithoutError() {
        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-token");

        service.logout(request);
    }

    @Test
    void logoutClientErrorThrowsInvalidCredentials() {
        when(requestBodySpec.retrieve()).thenThrow(clientError(HttpStatus.BAD_REQUEST, "invalid"));

        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-token");

        assertThatThrownBy(() -> service.logout(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void logoutServerErrorThrowsKeycloakOperationException() {
        when(requestBodySpec.retrieve())
                .thenThrow(HttpServerErrorException.create(HttpStatus.BAD_GATEWAY, "Bad Gateway", HttpHeaders.EMPTY, new byte[0], null));

        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-token");

        assertThatThrownBy(() -> service.logout(request))
                .isInstanceOf(KeycloakOperationException.class);
    }

    @Test
    void logoutConnectionFailureThrowsKeycloakOperationException() {
        when(requestBodySpec.retrieve()).thenThrow(new ResourceAccessException("baglanti kurulamadi"));

        LogoutRequest request = new LogoutRequest();
        request.setRefreshToken("refresh-token");

        assertThatThrownBy(() -> service.logout(request))
                .isInstanceOf(KeycloakOperationException.class);
    }
}
