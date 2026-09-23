package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.EnvCreateRequest;
import com.rs.gridservice.dto.EnvResponse;
import com.rs.gridservice.dto.EnvUpdateRequest;
import com.rs.gridservice.entity.EnvEntity;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.EnvRepository;
import com.rs.gridservice.service.EnvService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Env CRUD islemlerini kendi veritabanimizdaki "env" tablosuna karsi
 * gerceklestiren servis implementasyonu.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JpaEnvServiceImpl implements EnvService {

    private final EnvRepository envRepository;
    private final EntityManager entityManager;

    @Override
    public EnvResponse addEnv(EnvCreateRequest request) {
        EnvEntity entity = EnvEntity.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .url(request.getUrl())
                .userId(request.getUserId())
                .build();

        envRepository.save(entity);
        log.info("Env olusturuldu: id={}, name={}, url={}, userId={}",
                entity.getId(), entity.getName(), entity.getUrl(), entity.getUserId());
        return toResponse(entity);
    }

    @Override
    public EnvResponse getEnv(String envId) {
        return envRepository.findById(envId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Env bulunamadi: id=" + envId));
    }

    @Override
    public List<EnvResponse> getAllEnvs(String userId, int first, int max, String search) {
        boolean hasSearch = search != null && !search.isBlank();
        String jpql = hasSearch
                ? "SELECT e FROM EnvEntity e WHERE e.userId = :userId "
                        + "AND LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY e.name"
                : "SELECT e FROM EnvEntity e WHERE e.userId = :userId ORDER BY e.name";

        TypedQuery<EnvEntity> query = entityManager.createQuery(jpql, EnvEntity.class)
                .setParameter("userId", userId)
                .setFirstResult(first)
                .setMaxResults(max);
        if (hasSearch) {
            query.setParameter("search", search);
        }

        return query.getResultList().stream().map(this::toResponse).toList();
    }

    @Override
    public EnvResponse editEnv(String envId, String userId, EnvUpdateRequest request) {
        EnvEntity entity = findOwnedEnv(envId, userId);

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getUrl() != null) {
            entity.setUrl(request.getUrl());
        }
        if (request.getUserId() != null) {
            entity.setUserId(request.getUserId());
        }

        EnvEntity saved = envRepository.save(entity);
        log.info("Env guncellendi: id={}", envId);
        return toResponse(saved);
    }

    @Override
    public void deleteEnv(String envId, String userId) {
        EnvEntity entity = findOwnedEnv(envId, userId);
        envRepository.delete(entity);
        log.info("Env silindi: id={}", envId);
    }

    /** Kaydi bulur ve gercekten userId'ye ait oldugunu dogrular; degilse "bulunamadi" ile aynen davranir. */
    private EnvEntity findOwnedEnv(String envId, String userId) {
        return envRepository.findById(envId)
                .filter(entity -> entity.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Env bulunamadi: id=" + envId));
    }

    private EnvResponse toResponse(EnvEntity entity) {
        return EnvResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .url(entity.getUrl())
                .userId(entity.getUserId())
                .build();
    }
}
