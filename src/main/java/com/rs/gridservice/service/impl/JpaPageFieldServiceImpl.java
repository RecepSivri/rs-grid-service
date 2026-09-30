package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.PageFieldResponse;
import com.rs.gridservice.dto.PageFieldUpsertRequest;
import com.rs.gridservice.entity.PageEntity;
import com.rs.gridservice.entity.PageFieldEntity;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.PageFieldRepository;
import com.rs.gridservice.repository.PageRepository;
import com.rs.gridservice.service.PageFieldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Bir Page'in alan (PageField) listesini kendi veritabanimizdaki ("page_field"
 * tablosu) verilere karsi gerceklestiren servis implementasyonu. Tekil alan
 * CRUD'u yok -- UI ihtiyaci "listeyi duzenle, tumunu tek seferde kaydet"
 * oldugu icin replaceFields() ilgili page'in butun alanlarini silip yeniden
 * yazar (bkz. PageFieldController).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JpaPageFieldServiceImpl implements PageFieldService {

    private final PageFieldRepository pageFieldRepository;
    private final PageRepository pageRepository;

    @Override
    public List<PageFieldResponse> getFields(String pageId, String userId) {
        findOwnedPage(pageId, userId);
        return pageFieldRepository.findByPageIdOrderBySortOrderAsc(pageId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<PageFieldResponse> replaceFields(String pageId, String userId, List<PageFieldUpsertRequest> fields) {
        findOwnedPage(pageId, userId);

        pageFieldRepository.deleteByPageId(pageId);

        List<PageFieldEntity> entities = new ArrayList<>();
        int order = 0;
        for (PageFieldUpsertRequest field : fields) {
            entities.add(PageFieldEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .pageId(pageId)
                    .dataField(field.getDataField())
                    .caption(field.getCaption())
                    .inputType(field.getInputType())
                    .visible(field.getVisible())
                    .readonly(field.getReadonly())
                    .dataTypeId(field.getDataTypeId())
                    .lookupTargetPageId(field.getLookupTargetPageId())
                    .lookupValueField(field.getLookupValueField())
                    .lookupTemplate(field.getLookupTemplate())
                    .sortOrder(order++)
                    .build());
        }
        List<PageFieldEntity> saved = pageFieldRepository.saveAll(entities);
        log.info("Page alanlari kaydedildi: pageId={}, alanSayisi={}", pageId, saved.size());
        return saved.stream().map(this::toResponse).toList();
    }

    /** Kaydi bulur ve gercekten userId'ye ait oldugunu dogrular; degilse "bulunamadi" ile aynen davranir.
     * JpaPageServiceImpl'deki ayni yardimcinin bir kopyasi -- bu kodebase'te servisler birbirini degil,
     * dogrudan diger entity'nin repository'sini enjekte eder (bkz. JpaProjectTypeServiceImpl -> ProjectRepository). */
    private PageEntity findOwnedPage(String pageId, String userId) {
        return pageRepository.findById(pageId)
                .filter(entity -> entity.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Page bulunamadi: id=" + pageId));
    }

    private PageFieldResponse toResponse(PageFieldEntity entity) {
        return PageFieldResponse.builder()
                .id(entity.getId())
                .pageId(entity.getPageId())
                .dataField(entity.getDataField())
                .caption(entity.getCaption())
                .inputType(entity.getInputType())
                .visible(entity.isVisible())
                .readonly(entity.isReadonly())
                .dataTypeId(entity.getDataTypeId())
                .lookupTargetPageId(entity.getLookupTargetPageId())
                .lookupValueField(entity.getLookupValueField())
                .lookupTemplate(entity.getLookupTemplate())
                .sortOrder(entity.getSortOrder())
                .build();
    }
}
