package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.ProjectTypeCreateRequest;
import com.rs.gridservice.dto.ProjectTypeResponse;
import com.rs.gridservice.dto.ProjectTypeUpdateRequest;
import com.rs.gridservice.entity.ProjectTypeEntity;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.ProjectTypeRepository;
import com.rs.gridservice.service.ProjectTypeService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * ProjectType CRUD islemlerini kendi veritabanimizdaki (Keycloak'in kullandigi ayni Postgres,
 * "project_type" tablosu) verilere karsi gerceklestiren servis implementasyonu.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JpaProjectTypeServiceImpl implements ProjectTypeService {

    private final ProjectTypeRepository projectTypeRepository;
    private final EntityManager entityManager;

    @Override
    public ProjectTypeResponse addProjectType(ProjectTypeCreateRequest request) {
        ProjectTypeEntity entity = ProjectTypeEntity.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .build();

        projectTypeRepository.save(entity);
        log.info("ProjectType olusturuldu: id={}, name={}", entity.getId(), entity.getName());
        return toResponse(entity);
    }

    @Override
    public ProjectTypeResponse getProjectType(String projectTypeId) {
        return projectTypeRepository.findById(projectTypeId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectType bulunamadi: id=" + projectTypeId));
    }

    @Override
    public List<ProjectTypeResponse> getAllProjectTypes(int first, int max, String search) {
        boolean hasSearch = search != null && !search.isBlank();
        String jpql = hasSearch
                ? "SELECT t FROM ProjectTypeEntity t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY t.name"
                : "SELECT t FROM ProjectTypeEntity t ORDER BY t.name";

        TypedQuery<ProjectTypeEntity> query = entityManager.createQuery(jpql, ProjectTypeEntity.class)
                .setFirstResult(first)
                .setMaxResults(max);
        if (hasSearch) {
            query.setParameter("search", search);
        }

        return query.getResultList().stream().map(this::toResponse).toList();
    }

    @Override
    public ProjectTypeResponse editProjectType(String projectTypeId, ProjectTypeUpdateRequest request) {
        ProjectTypeEntity entity = projectTypeRepository.findById(projectTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("ProjectType bulunamadi: id=" + projectTypeId));

        entity.setName(request.getName());

        ProjectTypeEntity saved = projectTypeRepository.save(entity);
        log.info("ProjectType guncellendi: id={}", projectTypeId);
        return toResponse(saved);
    }

    @Override
    public void deleteProjectType(String projectTypeId) {
        if (!projectTypeRepository.existsById(projectTypeId)) {
            throw new ResourceNotFoundException("ProjectType bulunamadi: id=" + projectTypeId);
        }
        projectTypeRepository.deleteById(projectTypeId);
        log.info("ProjectType silindi: id={}", projectTypeId);
    }

    private ProjectTypeResponse toResponse(ProjectTypeEntity entity) {
        return ProjectTypeResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}
