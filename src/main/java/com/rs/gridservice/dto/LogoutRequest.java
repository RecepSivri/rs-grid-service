package com.rs.gridservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Cikis (logout) icin request body. Login'de donen refresh_token buraya verilir,
 * Keycloak o oturumu (session) gecersiz kilar.
 */
@Getter
@Setter
public class LogoutRequest {

    @NotBlank(message = "refreshToken bos olamaz")
    private String refreshToken;
}
