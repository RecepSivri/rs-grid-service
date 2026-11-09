package com.rs.gridservice.repository;

import com.rs.gridservice.entity.TestRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TestRunRepository extends JpaRepository<TestRunEntity, String> {

    Optional<TestRunEntity> findByProjectIdAndMode(String projectId, String mode);

    List<TestRunEntity> findByProjectIdIn(Collection<String> projectIds);

    void deleteByProjectIdAndMode(String projectId, String mode);
}
