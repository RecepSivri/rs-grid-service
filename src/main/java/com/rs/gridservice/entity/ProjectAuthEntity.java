package com.rs.gridservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * "project_auth" tablosuna karsilik gelen JPA entity'si. Bir Project'in, kendi harici
 * Ortam'larina (Env) attigi isteklere eklenecek Postman-tarzi Authorization bilgisini
 * (access/refresh token + yenileme adresi) tutar -- rs-screen-builder:shared/projectAuth.ts
 * ile 1:1. Proje basina TEK kayit oldugu icin ayri bir uuid id yerine dogrudan projectId
 * @Id olarak kullaniliyor (bkz. PageFieldEntity'nin aksine -- orada bir Page'in BIRDEN
 * FAZLA alani oldugu icin ayri id gerekiyordu, burada 1:1).
 */
@Entity
@Table(name = "project_auth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectAuthEntity {

    @Id
    @Column(name = "project_id")
    private String projectId;

    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "refresh_url")
    private String refreshUrl;

    @Column(name = "expires_at")
    private Long expiresAt;
}
