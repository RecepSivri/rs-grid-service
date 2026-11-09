package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.RegressionSuiteItemResponse;
import com.rs.gridservice.dto.RegressionSuiteItemUpsertRequest;
import com.rs.gridservice.entity.ProjectEntity;
import com.rs.gridservice.entity.RegressionSuiteItemEntity;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.ProjectRepository;
import com.rs.gridservice.repository.RegressionSuiteItemRepository;
import com.rs.gridservice.service.RegressionSuiteItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Bir projenin Regresyon Testi listesini kendi veritabanimizdaki
 * ("regression_suite_item" tablosu) verilere karsi gerceklestiren servis
 * implementasyonu. Tekil satir CRUD'u yok -- rs-screen-builder'daki "Ekle"/
 * "Sil"/surukle-birak/sira numarasi hepsi TARAYICIDA bir calisma kopyasi
 * uzerinde olur, "Kaydet"e basildiginda replaceItems() projenin butun
 * listesini silip YENIDEN yazar (bkz. JpaPageFieldServiceImpl.replaceFields --
 * AYNI "liste" deseni, PageField'in Page'i yerine burada Project var).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JpaRegressionSuiteItemServiceImpl implements RegressionSuiteItemService {

    private final RegressionSuiteItemRepository regressionSuiteItemRepository;
    private final ProjectRepository projectRepository;

    @Override
    public List<RegressionSuiteItemResponse> getItems(String projectId, String userId) {
        findOwnedProject(projectId, userId);
        return regressionSuiteItemRepository.findByProjectIdOrderBySortOrderAsc(projectId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<RegressionSuiteItemResponse> replaceItems(String projectId, String userId, List<RegressionSuiteItemUpsertRequest> items) {
        findOwnedProject(projectId, userId);

        regressionSuiteItemRepository.deleteByProjectId(projectId);

        List<RegressionSuiteItemEntity> entities = new ArrayList<>();
        int order = 0;
        for (RegressionSuiteItemUpsertRequest item : items) {
            entities.add(RegressionSuiteItemEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .projectId(projectId)
                    .pageId(item.getPageId())
                    .endpointLabel(item.getEndpointLabel())
                    .sortOrder(order++)
                    .build());
        }
        List<RegressionSuiteItemEntity> saved = regressionSuiteItemRepository.saveAll(entities);
        log.info("Regresyon listesi kaydedildi: projectId={}, satirSayisi={}", projectId, saved.size());
        return saved.stream().map(this::toResponse).toList();
    }

    /** Kaydi bulur ve gercekten userId'ye ait oldugunu dogrular; degilse "bulunamadi" ile aynen davranir.
     * JpaPageFieldServiceImpl.findOwnedPage ile AYNI desen -- bu kodebase'te servisler birbirini degil,
     * dogrudan diger entity'nin repository'sini enjekte eder. */
    private ProjectEntity findOwnedProject(String projectId, String userId) {
        return projectRepository.findById(projectId)
                .filter(entity -> entity.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Proje bulunamadi: id=" + projectId));
    }

    private RegressionSuiteItemResponse toResponse(RegressionSuiteItemEntity entity) {
        return RegressionSuiteItemResponse.builder()
                .id(entity.getId())
                .projectId(entity.getProjectId())
                .pageId(entity.getPageId())
                .endpointLabel(entity.getEndpointLabel())
                .sortOrder(entity.getSortOrder())
                .build();
    }
}
