package com.rs.gridservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * application.yml altindaki "rs-grid.keycloak" prefixli ayarlarin baglandigi sinif.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "rs-grid.keycloak")
public class KeycloakProperties {

    /** Localinizde calisan Keycloak'in base URL'i, orn: http://localhost:8080 */
    private String serverUrl;

    /** Kullanicilarin yonetilecegi realm adi */
    private String realm;

    /** Service account acik confidential client id */
    private String clientId;

    /** Confidential client secret */
    private String clientSecret;

    private int connectTimeoutMs = 5000;

    private int readTimeoutMs = 5000;

    private int poolSize = 10;
}
