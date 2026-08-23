package com.rs.gridservice.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

/**
 * Mevcut bir kullaniciyi guncellerken kullanilan request body.
 * Alanlar opsiyoneldir; null gonderilen alanlar degistirilmez (partial update).
 */
@Getter
@Setter
public class UserUpdateRequest {

    @Email(message = "gecerli bir email giriniz")
    private String email;

    private String firstName;

    private String lastName;

    private Boolean enabled;

    /** Verilirse kullanicinin sifresi bu deger ile resetlenir. */
    private String password;

    private Boolean temporaryPassword;
}
