package com.rs.gridservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Bir Page'in alan listesini toplu kaydederken (PUT) listenin her bir elemani
 * icin kullanilan request body. id/pageId/sortOrder istemciden alinmaz --
 * sunucu her kaydette listeyi bastan olusturur (bkz. JpaPageFieldServiceImpl).
 */
@Getter
@Setter
public class PageFieldUpsertRequest {

    @NotBlank(message = "dataField bos olamaz")
    private String dataField;

    private String caption;

    @NotBlank(message = "inputType bos olamaz")
    private String inputType;

    @NotNull(message = "visible bos olamaz")
    private Boolean visible;

    @NotNull(message = "readonly bos olamaz")
    private Boolean readonly;

    private String dataTypeId;
    private String lookupTargetPageId;
    private String lookupValueField;
    private String lookupTemplate;
}
