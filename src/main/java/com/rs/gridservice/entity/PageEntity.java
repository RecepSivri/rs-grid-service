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
 * "pages" tablosuna karsilik gelen JPA entity'si. Bir projeye ait ekran/sayfanin
 * CRUD/listeleme icin kullandigi API path'lerini tutar.
 */
@Entity
@Table(name = "pages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageEntity {

    @Id
    private String id;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String name;

    @Column(name = "get_api")
    private String getApi;

    @Column(name = "get_all_api")
    private String getAllApi;

    @Column(name = "delete_api")
    private String deleteApi;

    @Column(name = "add_api")
    private String addApi;

    @Column(name = "update_api")
    private String updateApi;

    @Column(name = "batch_update_api")
    private String batchUpdateApi;
}
