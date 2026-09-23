package com.rs.gridservice.controller;

import com.rs.gridservice.dto.PageCreateRequest;
import com.rs.gridservice.dto.PageResponse;
import com.rs.gridservice.dto.PageUpdateRequest;
import com.rs.gridservice.service.PageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * rs-grid-service'in disariya actigi page yonetim API'leri (CRUD).
 * Keycloak'i wrap etmez; kendi veritabanimizdaki "pages" tablosuna karsi calisir.
 */
@RestController
@RequestMapping("/api/v1/pages")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

    /** Yeni page kaydi ekle (add). */
    @PostMapping
    public ResponseEntity<PageResponse> addPage(@Valid @RequestBody PageCreateRequest request) {
        PageResponse created = pageService.addPage(request);
        return ResponseEntity.created(URI.create("/api/v1/pages/" + created.getId())).body(created);
    }

    /** Tek bir page kaydini getir (get). */
    @GetMapping("/{pageId}")
    public ResponseEntity<PageResponse> getPage(@PathVariable String pageId) {
        return ResponseEntity.ok(pageService.getPage(pageId));
    }

    /** projectId'ye ve userId'ye ait page kayitlarini listele, opsiyonel sayfalama ve isme gore arama (getAll). */
    @GetMapping
    public ResponseEntity<List<PageResponse>> getAllPages(
            @RequestParam String projectId,
            @RequestParam String userId,
            @RequestParam(defaultValue = "0") int first,
            @RequestParam(defaultValue = "50") int max,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(pageService.getAllPages(projectId, userId, first, max, search));
    }

    /** Page kaydini guncelle (edit); sadece kaydin sahibi (userId) guncelleyebilir. */
    @PutMapping("/{pageId}")
    public ResponseEntity<PageResponse> editPage(
            @PathVariable String pageId,
            @RequestParam String userId,
            @Valid @RequestBody PageUpdateRequest request) {
        return ResponseEntity.ok(pageService.editPage(pageId, userId, request));
    }

    /** Page kaydini sil (delete); sadece kaydin sahibi (userId) silebilir. */
    @DeleteMapping("/{pageId}")
    public ResponseEntity<Void> deletePage(
            @PathVariable String pageId,
            @RequestParam String userId) {
        pageService.deletePage(pageId, userId);
        return ResponseEntity.noContent().build();
    }
}
