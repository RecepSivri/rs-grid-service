package com.rs.gridservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ProjectAuthEntity'nin disariya acilan hali. rs-screen-builder:shared/projectAuth.ts'in
 * ProjectAuth arayuzuyle alan adlari birebir ayni (camelCase, JSON'da da ayni kalir).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAuthResponse {

    private String accessToken;
    private String refreshToken;
    private String refreshUrl;
    private Long expiresAt;
}
