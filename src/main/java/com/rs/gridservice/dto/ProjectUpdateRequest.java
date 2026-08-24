package com.rs.gridservice.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Mevcut bir projeyi guncellerken kullanilan request body.
 * Alanlar opsiyoneldir; null gonderilen alanlar degistirilmez (partial update).
 */
@Getter
@Setter
public class ProjectUpdateRequest {

    private String name;

    private String userId;

    private String technology;

    private String type;
}
