package com.rs.gridservice.exception;

/**
 * Keycloak Admin API'sinden beklenmeyen bir hata (5xx, baglanti hatasi vb.) dondugunde firlatilir.
 */
public class KeycloakOperationException extends RuntimeException {
    public KeycloakOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
