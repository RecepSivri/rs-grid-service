package com.rs.gridservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Mevcut bir project type'i guncellerken kullanilan request body.
 */
@Getter
@Setter
public class ProjectTypeUpdateRequest {

    @NotBlank(message = "name bos olamaz")
    private String name;
}
