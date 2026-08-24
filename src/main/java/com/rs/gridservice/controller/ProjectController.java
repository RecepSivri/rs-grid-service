package com.rs.gridservice.controller;

import com.rs.gridservice.dto.ProjectCreateRequest;
import com.rs.gridservice.dto.ProjectResponse;
import com.rs.gridservice.dto.ProjectUpdateRequest;
import com.rs.gridservice.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * rs-grid-service'in disariya actigi proje yonetim API'leri (CRUD).
 * Keycloak'i wrap etmez; kendi veritabanimizdaki "projects" tablosuna karsi calisir.
 */
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /** Yeni proje ekle (add). */
    @PostMapping
    public ResponseEntity<ProjectResponse> addProject(@Valid @RequestBody ProjectCreateRequest request) {
        ProjectResponse created = projectService.addProject(request);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + created.getId())).body(created);
    }

    /** Tek bir projeyi getir (get). */
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable String projectId) {
        return ResponseEntity.ok(projectService.getProject(projectId));
    }

    /** Tum projeleri listele, opsiyonel sayfalama ve isme gore arama (getAll). */
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects(
            @RequestParam(defaultValue = "0") int first,
            @RequestParam(defaultValue = "50") int max,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(projectService.getAllProjects(first, max, search));
    }

    /** Projeyi guncelle (edit). */
    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> editProject(
            @PathVariable String projectId,
            @Valid @RequestBody ProjectUpdateRequest request) {
        return ResponseEntity.ok(projectService.editProject(projectId, request));
    }

    /** Projeyi sil (delete). */
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable String projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }
}
