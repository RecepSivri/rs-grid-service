package com.rs.gridservice.repository;

import com.rs.gridservice.entity.PageFieldEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PageFieldRepository extends JpaRepository<PageFieldEntity, String> {

    List<PageFieldEntity> findByPageIdOrderBySortOrderAsc(String pageId);

    void deleteByPageId(String pageId);
}
