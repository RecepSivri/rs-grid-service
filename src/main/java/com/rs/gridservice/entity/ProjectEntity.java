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
 * "projects" tablosuna karsilik gelen JPA entity'si. Keycloak'in kullandigi ayni
 * Postgres database'inde, kendi tablomuzda tutulur (Keycloak'in kendi tablolarina dokunmaz).
 */
@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String technology;

    @Column(nullable = false)
    private String type;
}
