package com.rs.gridservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * "project_type" tablosuna karsilik gelen JPA entity'si.
 */
@Entity
@Table(name = "project_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectTypeEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;
}
