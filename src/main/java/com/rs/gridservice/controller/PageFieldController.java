package com.rs.gridservice.controller;

import com.rs.gridservice.dto.PageFieldResponse;
import com.rs.gridservice.dto.PageFieldUpsertRequest;
import com.rs.gridservice.service.PageFieldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Bir Page'in alan (PageField) listesini yonetir. Tekil alan CRUD'u yok --
 * GET tum listeyi getirir, PUT tum listeyi (ekleme/duzenleme/silme dahil)
 * tek seferde bastan yazar (bkz. JpaPageFieldServiceImpl.replaceFields).
 * Bu path zaten "/api/v1/pages/**" ile eslestigi icin ayrica bir
 * SecurityConfig kurali gerekmiyor.
 */
@RestController
@RequestMapping("/api/v1/pages/{pageId}/fields")
@RequiredArgsConstructor
public class PageFieldController {

    private final PageFieldService pageFieldService;

    @GetMapping
    public ResponseEntity<List<PageFieldResponse>> getFields(
            @PathVariable String pageId,
            @RequestParam String userId) {
        return ResponseEntity.ok(pageFieldService.getFields(pageId, userId));
    }

    @PutMapping
    public ResponseEntity<List<PageFieldResponse>> replaceFields(
            @PathVariable String pageId,
            @RequestParam String userId,
            @Valid @RequestBody List<PageFieldUpsertRequest> fields) {
        return ResponseEntity.ok(pageFieldService.replaceFields(pageId, userId, fields));
    }
}
