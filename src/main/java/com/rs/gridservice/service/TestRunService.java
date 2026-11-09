package com.rs.gridservice.service;

import com.rs.gridservice.dto.TestRunResponse;
import com.rs.gridservice.dto.TestRunUpsertRequest;

import java.util.List;

public interface TestRunService {

    /** Bir kullanicinin SAHIP OLDUGU her projenin, kaydedilmis her modunun
     * (Smoke/Regresyon) en son anlik goruntusu -- Dashboard'un tek cagrida
     * gosterdigi "Test Durumu" paneli icin. */
    List<TestRunResponse> getRunsForUser(String userId);

    TestRunResponse replaceRun(String projectId, String mode, String userId, TestRunUpsertRequest request);
}
