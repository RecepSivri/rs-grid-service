package com.rs.gridservice.repository;

import com.rs.gridservice.entity.ProjectTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectTypeRepository extends JpaRepository<ProjectTypeEntity, String> {
}
