package com.rs.gridservice.config;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Keycloak Admin REST API'sine (client_credentials grant ile) baglanacak
 * org.keycloak.admin.client.Keycloak bean'ini olusturur.
 *
 * Bu bean, KeycloakUserServiceImpl tarafindan Keycloak'in kullanici CRUD
 * API'lerini "wrap" etmek icin kullanilir.
 */
@Configuration
@EnableConfigurationProperties(KeycloakProperties.class)
public class KeycloakAdminClientConfig {

    @Bean(destroyMethod = "close")
    public Keycloak keycloakAdminClient(KeycloakProperties properties) {
        Client resteasyClient = ClientBuilder.newBuilder()
                .register(new KeycloakObjectMapperResolver())
                .build();

        return KeycloakBuilder.builder()
                .serverUrl(properties.getServerUrl())
                .realm(properties.getRealm())
                .clientId(properties.getClientId())
                .clientSecret(properties.getClientSecret())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .resteasyClient(resteasyClient)
                .build();
    }
}
