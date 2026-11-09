package com.rs.gridservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Bir calistirmanin (TestRunUpsertRequest.items) tek bir satiri. id/testRunId/
 * sortOrder istemciden alinmaz -- sunucu her kaydette listeyi bastan olusturur
 * (bkz. JpaTestRunServiceImpl.replaceRun, PageFieldUpsertRequest ile AYNI desen).
 */
@Getter
@Setter
public class TestRunItemUpsertRequest {

    @NotBlank(message = "pageId bos olamaz")
    private String pageId;

    @NotBlank(message = "pageName bos olamaz")
    private String pageName;

    @NotBlank(message = "endpointLabel bos olamaz")
    private String endpointLabel;

    @NotBlank(message = "verdict bos olamaz")
    private String verdict;

    @Size(max = 500, message = "detail en fazla 500 karakter olabilir")
    private String detail;
}
