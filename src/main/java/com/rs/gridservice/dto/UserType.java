package com.rs.gridservice.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

/**
 * Kullanicinin tipi. Keycloak'ta ayni isimli bir realm rolune karsilik gelir
 * (admin / web / desktop), kullanici olusturulurken atanir.
 *
 * Not: buyuk/kucuk harf donusumlerinde mutlaka Locale.ROOT kullanilir -
 * varsayilan (tr) locale'de "admin".toUpperCase() -> "ADMİN" (noktali I) uretip
 * enum eslesmesini bozuyordu.
 */
public enum UserType {
    ADMIN,
    WEB,
    DESKTOP;

    /** JSON'da kucuk harfle yazilir (orn. "admin"), Keycloak rol adiyla birebir eslesir. */
    @JsonValue
    public String toJson() {
        return name().toLowerCase(Locale.ROOT);
    }

    @JsonCreator
    public static UserType fromJson(String value) {
        return UserType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    public String roleName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
