package com.rs.gridservice.repository;

import com.rs.gridservice.entity.RegressionSuiteItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegressionSuiteItemRepository extends JpaRepository<RegressionSuiteItemEntity, String> {

    List<RegressionSuiteItemEntity> findByProjectIdOrderBySortOrderAsc(String projectId);

    void deleteByProjectId(String projectId);
}
