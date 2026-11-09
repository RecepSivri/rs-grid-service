package com.rs.gridservice.proxy;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProxySettingsTest {

    @Test
    void normalizesAllowedHostsTrimmingLowercasingAndDroppingBlanks() {
        ProxySettings settings = ProxySettings.of(true, List.of(" Api.Local ", "", "  ", "DB"), 1500, 2500, 10, 20);

        assertEquals(Set.of("api.local", "db"), settings.allowedHosts());
        assertTrue(settings.allowPrivateNetworks());
        assertEquals(Duration.ofMillis(1500), settings.connectTimeout());
        assertEquals(Duration.ofMillis(2500), settings.readTimeout());
        assertEquals(10, settings.maxRequestBytes());
        assertEquals(20, settings.maxResponseBytes());
    }

    @Test
    void defaultsToNoPrivateNetworkAccessWhenGivenFalse() {
        assertFalse(ProxySettings.of(false, List.of(), 1, 1, 1, 1).allowPrivateNetworks());
    }
}
