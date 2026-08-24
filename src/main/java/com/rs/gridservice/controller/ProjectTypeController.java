package com.rs.gridservice.controller;

import com.rs.gridservice.dto.ProjectTypeCreateRequest;
import com.rs.gridservice.dto.ProjectTypeResponse;
import com.rs.gridservice.dto.ProjectTypeUpdateRequest;
import com.rs.gridservice.service.ProjectTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * rs-grid-service'in disariya actigi project type yonetim API'leri (CRUD).
 * Keycloak'i wrap etmez; kendi veritabanimizdaki "project_type" tablosuna karsi calisir.
 */
@RestController
@RequestMapping("/api/v1/project-types")
@RequiredArgsConstructor
public class ProjectTypeController {

    private final ProjectTypeService projectTypeService;

    /** Yeni project type ekle (add). */
    @PostMapping
    public ResponseEntity<ProjectTypeResponse> addProjectType(@Valid @RequestBody ProjectTypeCreateRequest request) {
        ProjectTypeResponse created = projectTypeService.addProjectType(request);
        return ResponseEntity.created(URI.create("/api/v1/project-types/" + created.getId())).body(created);
    }

    /** Tek bir project type'i getir (get). */
    @GetMapping("/{projectTypeId}")
    public ResponseEntity<ProjectTypeResponse> getProjectType(@PathVariable String projectTypeId) {
        return ResponseEntity.ok(projectTypeService.getProjectType(projectTypeId));
    }

    /** Tum project type'leri listele, opsiyonel sayfalama ve isme gore arama (getAll). */
    @GetMapping
    public ResponseEntity<List<ProjectTypeResponse>> getAllProjectTypes(
            @RequestParam(defaultValue = "0") int first,
            @RequestParam(defaultValue = "50") int max,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(projectTypeService.getAllProjectTypes(first, max, search));
    }

    /** Project type'i guncelle (edit). */
    @PutMapping("/{projectTypeId}")
    public ResponseEntity<ProjectTypeResponse> editProjectType(
            @PathVariable String projectTypeId,
            @Valid @RequestBody ProjectTypeUpdateRequest request) {
        return ResponseEntity.ok(projectTypeService.editProjectType(projectTypeId, request));
    }

    /** Project type'i sil (delete). */
    @DeleteMapping("/{projectTypeId}")
    public ResponseEntity<Void> deleteProjectType(@PathVariable String projectTypeId) {
        projectTypeService.deleteProjectType(projectTypeId);
        return ResponseEntity.noContent().build();
    }
}
