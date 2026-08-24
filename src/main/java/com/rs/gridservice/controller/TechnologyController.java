package com.rs.gridservice.controller;

import com.rs.gridservice.dto.TechnologyCreateRequest;
import com.rs.gridservice.dto.TechnologyResponse;
import com.rs.gridservice.dto.TechnologyUpdateRequest;
import com.rs.gridservice.service.TechnologyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * rs-grid-service'in disariya actigi technology yonetim API'leri (CRUD).
 * Keycloak'i wrap etmez; kendi veritabanimizdaki "technology" tablosuna karsi calisir.
 */
@RestController
@RequestMapping("/api/v1/technologies")
@RequiredArgsConstructor
public class TechnologyController {

    private final TechnologyService technologyService;

    /** Yeni technology ekle (add). */
    @PostMapping
    public ResponseEntity<TechnologyResponse> addTechnology(@Valid @RequestBody TechnologyCreateRequest request) {
        TechnologyResponse created = technologyService.addTechnology(request);
        return ResponseEntity.created(URI.create("/api/v1/technologies/" + created.getId())).body(created);
    }

    /** Tek bir technology'yi getir (get). */
    @GetMapping("/{technologyId}")
    public ResponseEntity<TechnologyResponse> getTechnology(@PathVariable String technologyId) {
        return ResponseEntity.ok(technologyService.getTechnology(technologyId));
    }

    /** Tum technology'leri listele, opsiyonel sayfalama ve isme gore arama (getAll). */
    @GetMapping
    public ResponseEntity<List<TechnologyResponse>> getAllTechnologies(
            @RequestParam(defaultValue = "0") int first,
            @RequestParam(defaultValue = "50") int max,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(technologyService.getAllTechnologies(first, max, search));
    }

    /** Technology'yi guncelle (edit). */
    @PutMapping("/{technologyId}")
    public ResponseEntity<TechnologyResponse> editTechnology(
            @PathVariable String technologyId,
            @Valid @RequestBody TechnologyUpdateRequest request) {
        return ResponseEntity.ok(technologyService.editTechnology(technologyId, request));
    }

    /** Technology'yi sil (delete). */
    @DeleteMapping("/{technologyId}")
    public ResponseEntity<Void> deleteTechnology(@PathVariable String technologyId) {
        technologyService.deleteTechnology(technologyId);
        return ResponseEntity.noContent().build();
    }
}
