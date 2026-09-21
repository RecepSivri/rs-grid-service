package com.rs.gridservice.config;

import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakAdminClientConfigTest {

    private final KeycloakAdminClientConfig config = new KeycloakAdminClientConfig();

    @Test
    void buildsKeycloakAdminClientFromProperties() {
        KeycloakProperties properties = new KeycloakProperties();
        properties.setServerUrl("http://localhost:8080");
        properties.setRealm("user-service");
        properties.setClientId("rs-grid-service");
        properties.setClientSecret("secret");

        try (Keycloak keycloak = config.keycloakAdminClient(properties)) {
            assertThat(keycloak).isNotNull();
        }
    }

    @Test
    void buildsTokenRestClient() {
        RestClient restClient = config.keycloakTokenRestClient();

        assertThat(restClient).isNotNull();
    }
}
