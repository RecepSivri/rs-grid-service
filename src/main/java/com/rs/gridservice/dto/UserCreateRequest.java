package com.rs.gridservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Yeni bir Keycloak kullanicisi olustururken kullanilan request body.
 */
@Getter
@Setter
public class UserCreateRequest {

    @NotBlank(message = "username bos olamaz")
    private String username;

    @Email(message = "gecerli bir email giriniz")
    private String email;

    @NotBlank(message = "firstName bos olamaz")
    private String firstName;

    @NotBlank(message = "lastName bos olamaz")
    private String lastName;

    /** Kullanici olusturulduktan sonra aktif olsun mu? Varsayilan true. */
    private Boolean enabled = true;

    /**
     * Opsiyonel: verilirse kullaniciya bu sifre atanir.
     * Bos birakilirsa kullanici sifresiz olusturulur (orn. sadece SSO/federation ile giris).
     */
    private String password;

    /** true ise kullanici ilk girişte sifresini degistirmek zorunda kalir. */
    private Boolean temporaryPassword = true;

    /** Kullanicinin tipi: admin / web / desktop. Ayni isimli realm rolu olarak atanir. */
    @NotNull(message = "userType bos olamaz (admin, web veya desktop olmali)")
    private UserType userType;
}
