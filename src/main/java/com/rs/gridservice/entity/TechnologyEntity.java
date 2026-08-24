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
 * "technology" tablosuna karsilik gelen JPA entity'si.
 */
@Entity
@Table(name = "technology")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TechnologyEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;
}
