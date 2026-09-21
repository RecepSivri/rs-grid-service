package com.rs.gridservice.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Mevcut bir env kaydini guncellerken kullanilan request body.
 * Alanlar opsiyoneldir; null gonderilen alanlar degistirilmez (partial update).
 */
@Getter
@Setter
public class EnvUpdateRequest {

    private String name;

    private String url;

    private String userId;
}
