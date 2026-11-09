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
 * "regression_suite_item" tablosuna karsilik gelen JPA entity'si. Bir projenin
 * (Project) kullanicinin kendi secip sirayla dizdigi Regresyon Testi listesini
 * tutar -- her satir bir Page'in (pageId) tek bir API ucunu (endpointLabel,
 * pageEndpoints.ts'teki "GetAll"/"Get"/"Add"/"Update"/"Delete"/"BatchUpdate"
 * etiketlerinden biri) referanslar. userId burada YOK -- sahiplik PageField ile
 * AYNI desen: tek gercek sahip Project.userId, bkz. JpaRegressionSuiteItemServiceImpl.
 */
@Entity
@Table(name = "regression_suite_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegressionSuiteItemEntity {

    @Id
    private String id;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "page_id", nullable = false)
    private String pageId;

    @Column(name = "endpoint_label", nullable = false)
    private String endpointLabel;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
