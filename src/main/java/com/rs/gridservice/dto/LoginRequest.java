package com.rs.gridservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Kullanici girisi (login) icin request body.
 */
@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "username bos olamaz")
    private String username;

    @NotBlank(message = "password bos olamaz")
    private String password;
}
