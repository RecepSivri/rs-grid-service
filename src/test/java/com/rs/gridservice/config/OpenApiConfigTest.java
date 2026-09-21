package com.rs.gridservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void buildsOpenApiWithBearerJwtSecurityScheme() {
        OpenAPI openApi = new OpenApiConfig().rsGridServiceOpenApi();

        assertThat(openApi.getInfo().getTitle()).isEqualTo("rs-grid-service API");
        assertThat(openApi.getInfo().getVersion()).isEqualTo("v0.0.1");
        assertThat(openApi.getSecurity()).hasSize(1);
        assertThat(openApi.getSecurity().get(0)).containsKey("bearer-jwt");

        SecurityScheme scheme = openApi.getComponents().getSecuritySchemes().get("bearer-jwt");
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
    }
}
