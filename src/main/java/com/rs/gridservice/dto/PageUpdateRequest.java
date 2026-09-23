package com.rs.gridservice.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Mevcut bir page kaydini guncellerken kullanilan request body.
 * Alanlar opsiyoneldir; null gonderilen alanlar degistirilmez (partial update).
 */
@Getter
@Setter
public class PageUpdateRequest {

    private String projectId;

    private String userId;

    private String name;

    private String getApi;

    private String getAllApi;

    private String deleteApi;

    private String addApi;

    private String updateApi;

    private String batchUpdateApi;
}
