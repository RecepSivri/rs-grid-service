package com.rs.gridservice.proxy;

import java.time.Duration;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Giden istek proxy'sinin ayarlari (bkz. application.yml: rs-grid.proxy.*).
 *
 * @param allowPrivateNetworks true ise loopback ve ozel (10/8, 172.16/12, 192.168/16, fc00::/7 ...) adreslere
 *                             izin verilir. SADECE backend'in kullanicinin kendi makinesinde calistigi local
 *                             gelistirmede acilmali -- sunucuda "localhost" sunucunun kendisidir.
 *                             Link-local/metadata adresleri (169.254.0.0/16, fd00:ec2::254) bu ayardan bagimsiz
 *                             HER ZAMAN engelli.
 * @param allowedHosts         allowPrivateNetworks kapaliyken bile ozel adrese cozulmesine izin verilen host adlari.
 */
public record ProxySettings(
        boolean allowPrivateNetworks,
        Set<String> allowedHosts,
        Duration connectTimeout,
        Duration readTimeout,
        int maxRequestBytes,
        int maxResponseBytes) {

    public ProxySettings {
        allowedHosts = allowedHosts.stream()
                .map(h -> h.trim().toLowerCase(Locale.ROOT))
                .filter(h -> !h.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    public static ProxySettings of(boolean allowPrivateNetworks, Collection<String> allowedHosts,
                                   long connectTimeoutMs, long readTimeoutMs, int maxRequestBytes, int maxResponseBytes) {
        return new ProxySettings(allowPrivateNetworks, Set.copyOf(allowedHosts), Duration.ofMillis(connectTimeoutMs),
                Duration.ofMillis(readTimeoutMs), maxRequestBytes, maxResponseBytes);
    }
}
