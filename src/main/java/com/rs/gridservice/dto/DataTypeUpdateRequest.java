package com.rs.gridservice.dto;

import com.rs.gridservice.entity.DataTypeKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Bir data type'i guncellerken kullanilan request body (tam-degistirme).
 */
@Getter
@Setter
public class DataTypeUpdateRequest {

    @NotBlank(message = "name bos olamaz")
    private String name;

    @NotNull(message = "kind bos olamaz")
    private DataTypeKind kind;
}
