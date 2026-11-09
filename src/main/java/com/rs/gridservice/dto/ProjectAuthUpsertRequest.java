package com.rs.gridservice.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * PUT /api/v1/projects/{projectId}/auth icin istek govdesi. Hicbir alan zorunlu degil --
 * bos string'ler gecerli (frontend'deki "Temizle" de ayni endpoint'i bos degerlerle cagirir,
 * bkz. shared/authTokenDialog.ts:handleClear).
 */
@Getter
@Setter
public class ProjectAuthUpsertRequest {

    private String accessToken;
    private String refreshToken;
    private String refreshUrl;
    private Long expiresAt;
}
