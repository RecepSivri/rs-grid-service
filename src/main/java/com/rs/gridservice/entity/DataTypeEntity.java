package com.rs.gridservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * "data_type" tablosuna karsilik gelen JPA entity'si. Admin'in yonettigi,
 * Sayfalar'daki alanlarin (PageField) hangi veri turunde oldugunu secmek icin
 * kullandigi katalog.
 */
@Entity
@Table(name = "data_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataTypeEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataTypeKind kind;
}
