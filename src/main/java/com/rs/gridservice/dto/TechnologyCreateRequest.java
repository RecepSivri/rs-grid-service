package com.rs.gridservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Yeni bir technology olustururken kullanilan request body.
 */
@Getter
@Setter
public class TechnologyCreateRequest {

    @NotBlank(message = "name bos olamaz")
    private String name;
}
