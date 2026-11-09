package com.rs.gridservice.controller;

import com.rs.gridservice.dto.ProjectAuthResponse;
import com.rs.gridservice.dto.ProjectAuthUpsertRequest;
import com.rs.gridservice.service.ProjectAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Bir Project'in Yetkilendirme (Authorization: access/refresh token + yenileme adresi)
 * bilgisini yonetir -- rs-screen-builder'in Sayfalar workbench'indeki 🔑 Yetkilendirme
 * diyaloguna karsilik gelir (bkz. shared/projectAuth.ts). Tekil kayit, liste yok --
 * GET hic kayit yoksa 404 degil, bos bir yanit doner (bkz. JpaProjectAuthServiceImpl).
 * Bu path zaten "/api/v1/projects/**" ile eslestigi icin ayrica bir SecurityConfig
 * kurali gerekmiyor.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/auth")
@RequiredArgsConstructor
public class ProjectAuthController {

    private final ProjectAuthService projectAuthService;

    @GetMapping
    public ResponseEntity<ProjectAuthResponse> getAuth(
            @PathVariable String projectId,
            @RequestParam String userId) {
        return ResponseEntity.ok(projectAuthService.getAuth(projectId, userId));
    }

    @PutMapping
    public ResponseEntity<ProjectAuthResponse> saveAuth(
            @PathVariable String projectId,
            @RequestParam String userId,
            @Valid @RequestBody ProjectAuthUpsertRequest request) {
        return ResponseEntity.ok(projectAuthService.saveAuth(projectId, userId, request));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearAuth(
            @PathVariable String projectId,
            @RequestParam String userId) {
        projectAuthService.clearAuth(projectId, userId);
        return ResponseEntity.noContent().build();
    }
}
