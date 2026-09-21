package com.rs.gridservice.exception;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionClassesTest {

    @Test
    void resourceNotFoundExceptionCarriesMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("bulunamadi");
        assertThat(ex.getMessage()).isEqualTo("bulunamadi");
    }

    @Test
    void userAlreadyExistsExceptionCarriesMessage() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("zaten var");
        assertThat(ex.getMessage()).isEqualTo("zaten var");
    }

    @Test
    void invalidCredentialsExceptionCarriesMessage() {
        InvalidCredentialsException ex = new InvalidCredentialsException("gecersiz");
        assertThat(ex.getMessage()).isEqualTo("gecersiz");
    }

    @Test
    void keycloakOperationExceptionCarriesMessageAndCause() {
        RuntimeException cause = new RuntimeException("baglanti hatasi");
        KeycloakOperationException ex = new KeycloakOperationException("keycloak hatasi", cause);
        assertThat(ex.getMessage()).isEqualTo("keycloak hatasi");
        assertThat(ex.getCause()).isSameAs(cause);
    }

    @Test
    void apiErrorThreeArgConstructorSetsTimestampAndNullFieldErrors() {
        ApiError error = new ApiError(404, "Not Found", "bulunamadi");
        assertThat(error.status()).isEqualTo(404);
        assertThat(error.error()).isEqualTo("Not Found");
        assertThat(error.message()).isEqualTo("bulunamadi");
        assertThat(error.fieldErrors()).isNull();
        assertThat(error.timestamp()).isNotNull();
    }

    @Test
    void apiErrorFourArgConstructorSetsFieldErrors() {
        Map<String, String> fieldErrors = Map.of("username", "bos olamaz");
        ApiError error = new ApiError(400, "Bad Request", "gecersiz istek", fieldErrors);
        assertThat(error.fieldErrors()).isEqualTo(fieldErrors);
        assertThat(error.timestamp()).isNotNull();
    }
}
