package com.rs.gridservice.service.impl;

import com.rs.gridservice.config.KeycloakProperties;
import com.rs.gridservice.dto.LoginRequest;
import com.rs.gridservice.dto.LogoutRequest;
import com.rs.gridservice.dto.TokenResponse;
import com.rs.gridservice.exception.InvalidCredentialsException;
import com.rs.gridservice.exception.KeycloakOperationException;
import com.rs.gridservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Keycloak'in kendi token endpoint'ini (Resource Owner Password Credentials grant)
 * kullanarak kullanici adi/sifre karsiligi JWT alan login servisi.
 *
 * Not: Keycloak Admin API'si (KeycloakAdminClientConfig) ile karistirilmamali;
 * bu servis Admin API'ye degil, dogrudan realm'in /protocol/openid-connect/token
 * ucuna gider.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAuthServiceImpl implements AuthService {

    private final KeycloakProperties keycloakProperties;

    private final RestClient restClient = RestClient.create();

    @Override
    public TokenResponse login(LoginRequest request) {
        String tokenUrl = keycloakProperties.getServerUrl()
                + "/realms/" + keycloakProperties.getRealm()
                + "/protocol/openid-connect/token";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", keycloakProperties.getClientId());
        form.add("client_secret", keycloakProperties.getClientSecret());
        form.add("username", request.getUsername());
        form.add("password", request.getPassword());

        try {
            TokenResponse tokenResponse = restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(TokenResponse.class);
            log.info("Kullanici girisi basarili: username={}", request.getUsername());
            return tokenResponse;
        } catch (HttpClientErrorException e) {
            log.warn("Giris basarisiz: username={}, keycloak status={}", request.getUsername(), e.getStatusCode());
            throw new InvalidCredentialsException("Kullanici adi veya sifre hatali");
        } catch (HttpServerErrorException | ResourceAccessException e) {
            throw new KeycloakOperationException("Keycloak'a baglanilamadi", e);
        }
    }

    @Override
    public void logout(LogoutRequest request) {
        String logoutUrl = keycloakProperties.getServerUrl()
                + "/realms/" + keycloakProperties.getRealm()
                + "/protocol/openid-connect/logout";

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", keycloakProperties.getClientId());
        form.add("client_secret", keycloakProperties.getClientSecret());
        form.add("refresh_token", request.getRefreshToken());

        try {
            restClient.post()
                    .uri(logoutUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Kullanici cikisi basarili");
        } catch (HttpClientErrorException e) {
            throw new InvalidCredentialsException("Refresh token gecersiz veya suresi dolmus");
        } catch (HttpServerErrorException | ResourceAccessException e) {
            throw new KeycloakOperationException("Keycloak'a baglanilamadi", e);
        }
    }
}
