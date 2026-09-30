package com.rs.gridservice.controller;

import com.rs.gridservice.dto.DataTypeCreateRequest;
import com.rs.gridservice.dto.DataTypeResponse;
import com.rs.gridservice.dto.DataTypeUpdateRequest;
import com.rs.gridservice.service.DataTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * rs-grid-service'in disariya actigi data type yonetim API'leri (CRUD).
 * Sayfalar'daki alanlarin (PageField) dataType secimi bu kataloga karsi yapilir.
 */
@RestController
@RequestMapping("/api/v1/data-types")
@RequiredArgsConstructor
public class DataTypeController {

    private final DataTypeService dataTypeService;

    /** Yeni data type ekle (add). */
    @PostMapping
    public ResponseEntity<DataTypeResponse> addDataType(@Valid @RequestBody DataTypeCreateRequest request) {
        DataTypeResponse created = dataTypeService.addDataType(request);
        return ResponseEntity.created(URI.create("/api/v1/data-types/" + created.getId())).body(created);
    }

    /** Tek bir data type'i getir (get). */
    @GetMapping("/{dataTypeId}")
    public ResponseEntity<DataTypeResponse> getDataType(@PathVariable String dataTypeId) {
        return ResponseEntity.ok(dataTypeService.getDataType(dataTypeId));
    }

    /** Tum data type'leri listele, opsiyonel sayfalama ve isme gore arama (getAll). */
    @GetMapping
    public ResponseEntity<List<DataTypeResponse>> getAllDataTypes(
            @RequestParam(defaultValue = "0") int first,
            @RequestParam(defaultValue = "50") int max,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(dataTypeService.getAllDataTypes(first, max, search));
    }

    /** Data type'i guncelle (edit). */
    @PutMapping("/{dataTypeId}")
    public ResponseEntity<DataTypeResponse> editDataType(
            @PathVariable String dataTypeId,
            @Valid @RequestBody DataTypeUpdateRequest request) {
        return ResponseEntity.ok(dataTypeService.editDataType(dataTypeId, request));
    }

    /** Data type'i sil (delete). */
    @DeleteMapping("/{dataTypeId}")
    public ResponseEntity<Void> deleteDataType(@PathVariable String dataTypeId) {
        dataTypeService.deleteDataType(dataTypeId);
        return ResponseEntity.noContent().build();
    }
}
