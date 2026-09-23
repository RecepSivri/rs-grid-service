package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.PageCreateRequest;
import com.rs.gridservice.dto.PageResponse;
import com.rs.gridservice.dto.PageUpdateRequest;
import com.rs.gridservice.entity.PageEntity;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.PageRepository;
import com.rs.gridservice.service.PageService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Page CRUD islemlerini kendi veritabanimizdaki "pages" tablosuna karsi
 * gerceklestiren servis implementasyonu.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JpaPageServiceImpl implements PageService {

    private final PageRepository pageRepository;
    private final EntityManager entityManager;

    @Override
    public PageResponse addPage(PageCreateRequest request) {
        PageEntity entity = PageEntity.builder()
                .id(UUID.randomUUID().toString())
                .projectId(request.getProjectId())
                .userId(request.getUserId())
                .name(request.getName())
                .getApi(request.getGetApi())
                .getAllApi(request.getGetAllApi())
                .deleteApi(request.getDeleteApi())
                .addApi(request.getAddApi())
                .updateApi(request.getUpdateApi())
                .batchUpdateApi(request.getBatchUpdateApi())
                .build();

        pageRepository.save(entity);
        log.info("Page olusturuldu: id={}, projectId={}, name={}, userId={}",
                entity.getId(), entity.getProjectId(), entity.getName(), entity.getUserId());
        return toResponse(entity);
    }

    @Override
    public PageResponse getPage(String pageId) {
        return pageRepository.findById(pageId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Page bulunamadi: id=" + pageId));
    }

    @Override
    public List<PageResponse> getAllPages(String projectId, String userId, int first, int max, String search) {
        boolean hasSearch = search != null && !search.isBlank();
        String jpql = hasSearch
                ? "SELECT p FROM PageEntity p WHERE p.projectId = :projectId AND p.userId = :userId "
                        + "AND LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY p.name"
                : "SELECT p FROM PageEntity p WHERE p.projectId = :projectId AND p.userId = :userId ORDER BY p.name";

        TypedQuery<PageEntity> query = entityManager.createQuery(jpql, PageEntity.class)
                .setParameter("projectId", projectId)
                .setParameter("userId", userId)
                .setFirstResult(first)
                .setMaxResults(max);
        if (hasSearch) {
            query.setParameter("search", search);
        }

        return query.getResultList().stream().map(this::toResponse).toList();
    }

    @Override
    public PageResponse editPage(String pageId, String userId, PageUpdateRequest request) {
        PageEntity entity = findOwnedPage(pageId, userId);

        if (request.getProjectId() != null) {
            entity.setProjectId(request.getProjectId());
        }
        if (request.getUserId() != null) {
            entity.setUserId(request.getUserId());
        }
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getGetApi() != null) {
            entity.setGetApi(request.getGetApi());
        }
        if (request.getGetAllApi() != null) {
            entity.setGetAllApi(request.getGetAllApi());
        }
        if (request.getDeleteApi() != null) {
            entity.setDeleteApi(request.getDeleteApi());
        }
        if (request.getAddApi() != null) {
            entity.setAddApi(request.getAddApi());
        }
        if (request.getUpdateApi() != null) {
            entity.setUpdateApi(request.getUpdateApi());
        }
        if (request.getBatchUpdateApi() != null) {
            entity.setBatchUpdateApi(request.getBatchUpdateApi());
        }

        PageEntity saved = pageRepository.save(entity);
        log.info("Page guncellendi: id={}", pageId);
        return toResponse(saved);
    }

    @Override
    public void deletePage(String pageId, String userId) {
        PageEntity entity = findOwnedPage(pageId, userId);
        pageRepository.delete(entity);
        log.info("Page silindi: id={}", pageId);
    }

    /** Kaydi bulur ve gercekten userId'ye ait oldugunu dogrular; degilse "bulunamadi" ile aynen davranir. */
    private PageEntity findOwnedPage(String pageId, String userId) {
        return pageRepository.findById(pageId)
                .filter(entity -> entity.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Page bulunamadi: id=" + pageId));
    }

    private PageResponse toResponse(PageEntity entity) {
        return PageResponse.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .userId(entity.getUserId())
                .name(entity.getName())
                .getApi(entity.getGetApi())
                .getAllApi(entity.getGetAllApi())
                .deleteApi(entity.getDeleteApi())
                .addApi(entity.getAddApi())
                .updateApi(entity.getUpdateApi())
                .batchUpdateApi(entity.getBatchUpdateApi())
                .build();
    }
}
