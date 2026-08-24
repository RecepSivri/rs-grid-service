package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.ProjectCreateRequest;
import com.rs.gridservice.dto.ProjectResponse;
import com.rs.gridservice.dto.ProjectUpdateRequest;
import com.rs.gridservice.entity.ProjectEntity;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.ProjectRepository;
import com.rs.gridservice.service.ProjectService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Project CRUD islemlerini kendi veritabanimizdaki (Keycloak'in kullandigi ayni Postgres,
 * "projects" tablosu) verilere karsi gerceklestiren servis implementasyonu.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JpaProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final EntityManager entityManager;

    @Override
    public ProjectResponse addProject(ProjectCreateRequest request) {
        ProjectEntity entity = ProjectEntity.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .userId(request.getUserId())
                .technology(request.getTechnology())
                .type(request.getType())
                .build();

        projectRepository.save(entity);
        log.info("Proje olusturuldu: id={}, name={}, userId={}", entity.getId(), entity.getName(), entity.getUserId());
        return toResponse(entity);
    }

    @Override
    public ProjectResponse getProject(String projectId) {
        return projectRepository.findById(projectId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Proje bulunamadi: id=" + projectId));
    }

    @Override
    public List<ProjectResponse> getAllProjects(int first, int max, String search) {
        boolean hasSearch = search != null && !search.isBlank();
        String jpql = hasSearch
                ? "SELECT p FROM ProjectEntity p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY p.name"
                : "SELECT p FROM ProjectEntity p ORDER BY p.name";

        TypedQuery<ProjectEntity> query = entityManager.createQuery(jpql, ProjectEntity.class)
                .setFirstResult(first)
                .setMaxResults(max);
        if (hasSearch) {
            query.setParameter("search", search);
        }

        return query.getResultList().stream().map(this::toResponse).toList();
    }

    @Override
    public ProjectResponse editProject(String projectId, ProjectUpdateRequest request) {
        ProjectEntity entity = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Proje bulunamadi: id=" + projectId));

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getUserId() != null) {
            entity.setUserId(request.getUserId());
        }
        if (request.getTechnology() != null) {
            entity.setTechnology(request.getTechnology());
        }
        if (request.getType() != null) {
            entity.setType(request.getType());
        }

        ProjectEntity saved = projectRepository.save(entity);
        log.info("Proje guncellendi: id={}", projectId);
        return toResponse(saved);
    }

    @Override
    public void deleteProject(String projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Proje bulunamadi: id=" + projectId);
        }
        projectRepository.deleteById(projectId);
        log.info("Proje silindi: id={}", projectId);
    }

    private ProjectResponse toResponse(ProjectEntity entity) {
        return ProjectResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .userId(entity.getUserId())
                .technology(entity.getTechnology())
                .type(entity.getType())
                .build();
    }
}
