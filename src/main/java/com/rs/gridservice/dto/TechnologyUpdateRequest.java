package com.rs.gridservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Mevcut bir technology'yi guncellerken kullanilan request body.
 */
@Getter
@Setter
public class TechnologyUpdateRequest {

    @NotBlank(message = "name bos olamaz")
    private String name;
}
