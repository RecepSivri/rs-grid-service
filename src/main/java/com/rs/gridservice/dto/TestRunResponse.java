package com.rs.gridservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * TestRunEntity'nin disariya acilan hali. "problems" SADECE pass OLMAYAN
 * satirlari tasir (bkz. JpaTestRunServiceImpl) -- gecen satirlarin sayisi
 * passCount'ta zaten var, tek tek geri okunmaz (buyuk, hepsi yesil bir
 * calistirma Dashboard'a bos bir dizi olarak doner).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRunResponse {

    private String id;
    private String projectId;
    private String mode;
    private long runAtEpochMs;
    private String envName;
    private String envUrl;
    private int passCount;
    private int failCount;
    private int errorCount;
    private int skippedCount;
    /** Regresyon icin: kaydedilen satirlarin kumesi, suite'in SU ANKI kumesinden
     * farkliysa true (bkz. JpaTestRunServiceImpl#computeSuiteChanged) -- Smoke'ta
     * (ve hesaplanamadiginda) null, frontend null'i "rozet gosterme" olarak okur. */
    private Boolean suiteChanged;
    private List<TestRunItemResponse> problems;
}
