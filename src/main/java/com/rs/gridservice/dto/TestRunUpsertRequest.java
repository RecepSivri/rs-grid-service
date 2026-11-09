package com.rs.gridservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * "Tümünü Çalıştır" tamamlandiginda projenin TEK bir modu (Smoke/Regresyon)
 * icin gonderilen anlik goruntu -- bkz. JpaTestRunServiceImpl.replaceRun.
 * Tekil satir calistirmalari (handleRunRow) bu uc noktayi HIC cagirmaz.
 */
@Getter
@Setter
public class TestRunUpsertRequest {

    @NotBlank(message = "envUrl bos olamaz")
    private String envUrl;

    private String envName;

    @NotNull(message = "items bos olamaz")
    @Valid
    private List<TestRunItemUpsertRequest> items;
}
