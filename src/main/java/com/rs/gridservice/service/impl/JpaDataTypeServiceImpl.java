package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.DataTypeCreateRequest;
import com.rs.gridservice.dto.DataTypeResponse;
import com.rs.gridservice.dto.DataTypeUpdateRequest;
import com.rs.gridservice.entity.DataTypeEntity;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.DataTypeRepository;
import com.rs.gridservice.service.DataTypeService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * DataType CRUD islemlerini kendi veritabanimizdaki ("data_type" tablosu)
 * verilere karsi gerceklestiren servis implementasyonu.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JpaDataTypeServiceImpl implements DataTypeService {

    private final DataTypeRepository dataTypeRepository;
    private final EntityManager entityManager;

    @Override
    public DataTypeResponse addDataType(DataTypeCreateRequest request) {
        DataTypeEntity entity = DataTypeEntity.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .kind(request.getKind())
                .build();

        dataTypeRepository.save(entity);
        log.info("DataType olusturuldu: id={}, name={}, kind={}", entity.getId(), entity.getName(), entity.getKind());
        return toResponse(entity);
    }

    @Override
    public DataTypeResponse getDataType(String dataTypeId) {
        return dataTypeRepository.findById(dataTypeId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("DataType bulunamadi: id=" + dataTypeId));
    }

    @Override
    public List<DataTypeResponse> getAllDataTypes(int first, int max, String search) {
        boolean hasSearch = search != null && !search.isBlank();
        String jpql = hasSearch
                ? "SELECT t FROM DataTypeEntity t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY t.name"
                : "SELECT t FROM DataTypeEntity t ORDER BY t.name";

        TypedQuery<DataTypeEntity> query = entityManager.createQuery(jpql, DataTypeEntity.class)
                .setFirstResult(first)
                .setMaxResults(max);
        if (hasSearch) {
            query.setParameter("search", search);
        }

        return query.getResultList().stream().map(this::toResponse).toList();
    }

    @Override
    public DataTypeResponse editDataType(String dataTypeId, DataTypeUpdateRequest request) {
        DataTypeEntity entity = dataTypeRepository.findById(dataTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("DataType bulunamadi: id=" + dataTypeId));

        entity.setName(request.getName());
        entity.setKind(request.getKind());

        DataTypeEntity saved = dataTypeRepository.save(entity);
        log.info("DataType guncellendi: id={}", dataTypeId);
        return toResponse(saved);
    }

    @Override
    public void deleteDataType(String dataTypeId) {
        if (!dataTypeRepository.existsById(dataTypeId)) {
            throw new ResourceNotFoundException("DataType bulunamadi: id=" + dataTypeId);
        }
        dataTypeRepository.deleteById(dataTypeId);
        log.info("DataType silindi: id={}", dataTypeId);
    }

    private DataTypeResponse toResponse(DataTypeEntity entity) {
        return DataTypeResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .kind(entity.getKind())
                .build();
    }
}
