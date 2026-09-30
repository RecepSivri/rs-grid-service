package com.rs.gridservice.dto;

import com.rs.gridservice.entity.DataTypeKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Yeni bir data type olustururken kullanilan request body.
 */
@Getter
@Setter
public class DataTypeCreateRequest {

    @NotBlank(message = "name bos olamaz")
    private String name;

    @NotNull(message = "kind bos olamaz")
    private DataTypeKind kind;
}
