package com.rs.gridservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Yeni bir env kaydi olustururken kullanilan request body.
 */
@Getter
@Setter
public class EnvCreateRequest {

    @NotBlank(message = "name bos olamaz")
    private String name;

    @NotBlank(message = "url bos olamaz")
    private String url;

    @NotBlank(message = "userId bos olamaz")
    private String userId;
}
