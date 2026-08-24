package com.rs.gridservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Yeni bir proje olustururken kullanilan request body.
 */
@Getter
@Setter
public class ProjectCreateRequest {

    @NotBlank(message = "name bos olamaz")
    private String name;

    @NotBlank(message = "userId bos olamaz")
    private String userId;

    @NotBlank(message = "technology bos olamaz")
    private String technology;

    @NotBlank(message = "type bos olamaz")
    private String type;
}
