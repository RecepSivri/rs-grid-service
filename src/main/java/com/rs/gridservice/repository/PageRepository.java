package com.rs.gridservice.repository;

import com.rs.gridservice.entity.PageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageRepository extends JpaRepository<PageEntity, String> {
}
