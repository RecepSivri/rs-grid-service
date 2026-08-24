package com.rs.gridservice.repository;

import com.rs.gridservice.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<ProjectEntity, String> {
}
