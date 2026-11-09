package com.rs.gridservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Bir projenin Regresyon Testi listesini toplu kaydederken (PUT) listenin her
 * bir elemani icin kullanilan request body. id/projectId/sortOrder istemciden
 * alinmaz -- sunucu her kaydette listeyi bastan olusturur (bkz.
 * JpaRegressionSuiteItemServiceImpl), sortOrder listedeki pozisyondan gelir.
 */
@Getter
@Setter
public class RegressionSuiteItemUpsertRequest {

    @NotBlank(message = "pageId bos olamaz")
    private String pageId;

    @NotBlank(message = "endpointLabel bos olamaz")
    private String endpointLabel;
}
