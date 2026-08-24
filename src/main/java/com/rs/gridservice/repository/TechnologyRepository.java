package com.rs.gridservice.repository;

import com.rs.gridservice.entity.TechnologyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechnologyRepository extends JpaRepository<TechnologyEntity, String> {
}
