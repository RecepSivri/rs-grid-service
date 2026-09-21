package com.rs.gridservice.controller;

import com.rs.gridservice.dto.EnvCreateRequest;
import com.rs.gridservice.dto.EnvResponse;
import com.rs.gridservice.dto.EnvUpdateRequest;
import com.rs.gridservice.service.EnvService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * rs-grid-service'in disariya actigi env yonetim API'leri (CRUD).
 * Keycloak'i wrap etmez; kendi veritabanimizdaki "env" tablosuna karsi calisir.
 */
@RestController
@RequestMapping("/api/v1/envs")
@RequiredArgsConstructor
public class EnvController {

    private final EnvService envService;

    /** Yeni env kaydi ekle (add). */
    @PostMapping
    public ResponseEntity<EnvResponse> addEnv(@Valid @RequestBody EnvCreateRequest request) {
        EnvResponse created = envService.addEnv(request);
        return ResponseEntity.created(URI.create("/api/v1/envs/" + created.getId())).body(created);
    }

    /** Tek bir env kaydini getir (get). */
    @GetMapping("/{envId}")
    public ResponseEntity<EnvResponse> getEnv(@PathVariable String envId) {
        return ResponseEntity.ok(envService.getEnv(envId));
    }

    /** Tum env kayitlarini listele, opsiyonel sayfalama ve isme gore arama (getAll). */
    @GetMapping
    public ResponseEntity<List<EnvResponse>> getAllEnvs(
            @RequestParam(defaultValue = "0") int first,
            @RequestParam(defaultValue = "50") int max,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(envService.getAllEnvs(first, max, search));
    }

    /** Env kaydini guncelle (edit). */
    @PutMapping("/{envId}")
    public ResponseEntity<EnvResponse> editEnv(
            @PathVariable String envId,
            @Valid @RequestBody EnvUpdateRequest request) {
        return ResponseEntity.ok(envService.editEnv(envId, request));
    }

    /** Env kaydini sil (delete). */
    @DeleteMapping("/{envId}")
    public ResponseEntity<Void> deleteEnv(@PathVariable String envId) {
        envService.deleteEnv(envId);
        return ResponseEntity.noContent().build();
    }
}
