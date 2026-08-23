package com.rs.gridservice.exception;

/**
 * Kullanici adi/sifre hatali oldugunda (Keycloak'in invalid_grant donusu) firlatilir.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
