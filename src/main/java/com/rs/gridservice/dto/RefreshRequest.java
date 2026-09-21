package com.rs.gridservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Token yenileme (refresh) icin request body.
 * Login'de donen refresh_token buraya verilir, karsiliginda yeni bir
 * access_token (+ genellikle yeni bir refresh_token) alinir.
 */
@Getter
@Setter
public class RefreshRequest {

    @NotBlank(message = "refreshToken bos olamaz")
    private String refreshToken;
}
