package com.rs.gridservice.repository;

import com.rs.gridservice.entity.TestRunItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TestRunItemRepository extends JpaRepository<TestRunItemEntity, String> {

    /** Dashboard'un toplu okumasi (getRunsForUser) icin: PASS OLMAYANLARI, birden
     * fazla calistirma id'si uzerinden tek sorguda getirir -- gecen satirlar hic
     * okunmaz (bkz. TestRunResponse.problems, sadece sorun satirlari tasir). */
    List<TestRunItemEntity> findByTestRunIdInAndVerdictNotOrderBySortOrderAsc(Collection<String> testRunIds, String verdict);

    void deleteByTestRunId(String testRunId);
}
