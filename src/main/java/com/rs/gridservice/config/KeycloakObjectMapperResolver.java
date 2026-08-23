package com.rs.gridservice.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * keycloak-admin-client'in kullandigi Jackson ObjectMapper'i bilinmeyen JSON
 * alanlarini gormezden gelecek sekilde yapilandirir.
 *
 * Keycloak sunucusu (bu ortamda 26.x), kullandigimiz keycloak-admin-client
 * kutuphanesinden (25.0.6) daha yeni oldugu icin bazi representation'lara
 * (orn. GroupRepresentation.description) yeni alanlar eklemis olabilir.
 * Varsayilan strict deserialization bu durumda UnrecognizedPropertyException
 * firlatiyordu; bu resolver onu engeller.
 */
@Provider
public class KeycloakObjectMapperResolver implements ContextResolver<ObjectMapper> {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }
}
