package com.rs.gridservice.config;

import com.rs.gridservice.proxy.OutboundProxyService;
import com.rs.gridservice.proxy.ProxySettings;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProxyConfigTest {

    private final ProxyConfig config = new ProxyConfig();

    @Test
    void buildsSettingsFromPropertyValues() {
        ProxySettings settings = config.proxySettings(true, List.of("Api.Local"), 1000, 2000, 300, 400);

        assertTrue(settings.allowPrivateNetworks());
        assertEquals(Set.of("api.local"), settings.allowedHosts());
        assertEquals(Duration.ofMillis(1000), settings.connectTimeout());
        assertEquals(Duration.ofMillis(2000), settings.readTimeout());
        assertEquals(300, settings.maxRequestBytes());
        assertEquals(400, settings.maxResponseBytes());
    }

    @Test
    void createsTheProxyServiceFromSettings() {
        OutboundProxyService service = config.outboundProxyService(config.proxySettings(false, List.of(), 1000, 2000, 300, 400));

        assertNotNull(service);
    }
}
