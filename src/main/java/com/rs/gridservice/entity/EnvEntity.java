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
 * "env" tablosuna karsilik gelen JPA entity'si.
 */
@Entity
@Table(name = "env")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String url;

    @Column(name = "user_id", nullable = false)
    private String userId;
}
