package com.rs.gridservice.config;

import com.rs.gridservice.proxy.OutboundProxyService;
import com.rs.gridservice.proxy.ProxySettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** /api/v1/proxy ayarlari (application.yml: rs-grid.proxy.*). */
@Configuration
public class ProxyConfig {

    @Bean
    public ProxySettings proxySettings(
            @Value("${rs-grid.proxy.allow-private-networks:false}") boolean allowPrivateNetworks,
            @Value("${rs-grid.proxy.allowed-hosts:}") List<String> allowedHosts,
            @Value("${rs-grid.proxy.connect-timeout-ms:5000}") long connectTimeoutMs,
            @Value("${rs-grid.proxy.read-timeout-ms:30000}") long readTimeoutMs,
            @Value("${rs-grid.proxy.max-request-bytes:2097152}") int maxRequestBytes,
            @Value("${rs-grid.proxy.max-response-bytes:5242880}") int maxResponseBytes) {
        return ProxySettings.of(allowPrivateNetworks, allowedHosts, connectTimeoutMs, readTimeoutMs, maxRequestBytes, maxResponseBytes);
    }

    @Bean
    public OutboundProxyService outboundProxyService(ProxySettings settings) {
        return new OutboundProxyService(settings);
    }
}
