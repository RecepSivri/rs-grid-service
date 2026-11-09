package com.rs.gridservice.controller;

import com.rs.gridservice.dto.ProxyRequest;
import com.rs.gridservice.dto.ProxyResponse;
import com.rs.gridservice.proxy.OutboundProxyService;
import com.rs.gridservice.proxy.ProxyResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * rs-screen-builder'in tarayici modunda hedef REST API'ye istek atabilmesi icin (CORS'u asmak) proxy.
 * Yetki: "admin" veya "desktop_user" (bkz. SecurityConfig). Hedef kisitlari ve sinirlar icin bkz.
 * proxy.TargetPolicy / proxy.OutboundProxyService.
 */
@RestController
@RequestMapping("/api/v1/proxy")
public class ProxyController {

    private static final Logger log = LoggerFactory.getLogger(ProxyController.class);

    private final OutboundProxyService proxyService;

    public ProxyController(OutboundProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @PostMapping
    public ResponseEntity<ProxyResponse> forward(@Valid @RequestBody ProxyRequest request) {
        ProxyResult result = proxyService.execute(request.method(), request.url(), request.headers(), request.body());
        // Yalnizca host loglanir: yol, sorgu, header ve govde token/kisisel veri tasiyabilir.
        log.info("proxy host={} status={} elapsedMs={}", URI.create(request.url().trim()).getHost(), result.status(), result.elapsedMs());

        HttpStatus known = HttpStatus.resolve(result.status());
        List<ProxyResponse.Header> headers = result.headers().stream()
                .map(h -> new ProxyResponse.Header(h.key(), h.value()))
                .toList();
        return ResponseEntity.ok(new ProxyResponse(
                result.status(), known == null ? "" : known.getReasonPhrase(), headers, result.body(), result.elapsedMs()));
    }
}
