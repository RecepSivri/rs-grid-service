package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.TechnologyCreateRequest;
import com.rs.gridservice.dto.TechnologyResponse;
import com.rs.gridservice.dto.TechnologyUpdateRequest;
import com.rs.gridservice.entity.TechnologyEntity;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.TechnologyRepository;
import com.rs.gridservice.service.TechnologyService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Technology CRUD islemlerini kendi veritabanimizdaki (Keycloak'in kullandigi ayni Postgres,
 * "technology" tablosu) verilere karsi gerceklestiren servis implementasyonu.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JpaTechnologyServiceImpl implements TechnologyService {

    private final TechnologyRepository technologyRepository;
    private final EntityManager entityManager;

    @Override
    public TechnologyResponse addTechnology(TechnologyCreateRequest request) {
        TechnologyEntity entity = TechnologyEntity.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .build();

        technologyRepository.save(entity);
        log.info("Technology olusturuldu: id={}, name={}", entity.getId(), entity.getName());
        return toResponse(entity);
    }

    @Override
    public TechnologyResponse getTechnology(String technologyId) {
        return technologyRepository.findById(technologyId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Technology bulunamadi: id=" + technologyId));
    }

    @Override
    public List<TechnologyResponse> getAllTechnologies(int first, int max, String search) {
        boolean hasSearch = search != null && !search.isBlank();
        String jpql = hasSearch
                ? "SELECT t FROM TechnologyEntity t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY t.name"
                : "SELECT t FROM TechnologyEntity t ORDER BY t.name";

        TypedQuery<TechnologyEntity> query = entityManager.createQuery(jpql, TechnologyEntity.class)
                .setFirstResult(first)
                .setMaxResults(max);
        if (hasSearch) {
            query.setParameter("search", search);
        }

        return query.getResultList().stream().map(this::toResponse).toList();
    }

    @Override
    public TechnologyResponse editTechnology(String technologyId, TechnologyUpdateRequest request) {
        TechnologyEntity entity = technologyRepository.findById(technologyId)
                .orElseThrow(() -> new ResourceNotFoundException("Technology bulunamadi: id=" + technologyId));

        entity.setName(request.getName());

        TechnologyEntity saved = technologyRepository.save(entity);
        log.info("Technology guncellendi: id={}", technologyId);
        return toResponse(saved);
    }

    @Override
    public void deleteTechnology(String technologyId) {
        if (!technologyRepository.existsById(technologyId)) {
            throw new ResourceNotFoundException("Technology bulunamadi: id=" + technologyId);
        }
        technologyRepository.deleteById(technologyId);
        log.info("Technology silindi: id={}", technologyId);
    }

    private TechnologyResponse toResponse(TechnologyEntity entity) {
        return TechnologyResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }
}
