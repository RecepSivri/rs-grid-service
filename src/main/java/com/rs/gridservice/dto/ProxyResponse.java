package com.rs.gridservice.dto;

import java.util.List;

/**
 * Hedef sunucunun yanitinin zarfi. Hedefin 4xx/5xx yanitlari da burada {@code status} olarak doner
 * (proxy cagrisi kendisi 200 doner); proxy'nin kendi reddi/hatasi ise ApiError ile 4xx/5xx doner.
 */
public record ProxyResponse(int status, String statusText, List<Header> headers, String body, long elapsedMs) {

    public record Header(String key, String value) {
    }
}
