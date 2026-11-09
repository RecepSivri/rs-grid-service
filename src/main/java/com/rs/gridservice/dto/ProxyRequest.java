package com.rs.gridservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * Tarayici modunda hedef API'ye gidecek istek (bkz. proxy.OutboundProxyService).
 * {@code headers} HEDEF sunucuya gidecek basliklardir -- bu uygulamanin kendi oturum token'i
 * (Authorization: Bearer ...) proxy istegini yetkilendirir ve hedefe ILETILMEZ.
 */
public record ProxyRequest(
        @NotBlank String method,
        @NotBlank @Size(max = 2048) String url,
        Map<String, String> headers,
        String body) {
}
