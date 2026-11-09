package com.rs.gridservice.controller;

import com.rs.gridservice.dto.RegressionSuiteItemResponse;
import com.rs.gridservice.dto.RegressionSuiteItemUpsertRequest;
import com.rs.gridservice.service.RegressionSuiteItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Bir projenin Regresyon Testi listesini yonetir. Tekil satir CRUD'u yok --
 * GET tum listeyi (sirali) getirir, PUT tum listeyi (ekleme/cikarma/yeniden
 * siralama dahil) tek seferde bastan yazar (bkz.
 * JpaRegressionSuiteItemServiceImpl.replaceItems). Bu path zaten
 * "/api/v1/projects/**" ile eslestigi icin ayrica bir SecurityConfig kurali
 * gerekmiyor.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/regression-suite-items")
@RequiredArgsConstructor
public class RegressionSuiteItemController {

    private final RegressionSuiteItemService regressionSuiteItemService;

    @GetMapping
    public ResponseEntity<List<RegressionSuiteItemResponse>> getItems(
            @PathVariable String projectId,
            @RequestParam String userId) {
        return ResponseEntity.ok(regressionSuiteItemService.getItems(projectId, userId));
    }

    @PutMapping
    public ResponseEntity<List<RegressionSuiteItemResponse>> replaceItems(
            @PathVariable String projectId,
            @RequestParam String userId,
            @Valid @RequestBody List<RegressionSuiteItemUpsertRequest> items) {
        return ResponseEntity.ok(regressionSuiteItemService.replaceItems(projectId, userId, items));
    }
}
