package com.rs.gridservice.repository;

import com.rs.gridservice.entity.DataTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataTypeRepository extends JpaRepository<DataTypeEntity, String> {
}
