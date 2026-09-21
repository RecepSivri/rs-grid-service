package com.rs.gridservice.repository;

import com.rs.gridservice.entity.EnvEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnvRepository extends JpaRepository<EnvEntity, String> {
}
