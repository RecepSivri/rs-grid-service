package com.rs.gridservice.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakObjectMapperResolverTest {

    @Test
    void returnsObjectMapperThatIgnoresUnknownProperties() {
        ObjectMapper mapper = new KeycloakObjectMapperResolver().getContext(String.class);

        assertThat(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
    }
}
