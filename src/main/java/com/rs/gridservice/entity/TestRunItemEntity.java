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
 * "test_run_item" tablosuna karsilik gelen JPA entity'si -- bir TestRunEntity'nin
 * (proje+mod anlik goruntusu) icindeki tek bir uc nokta sonucu. pageName
 * KASITLI olarak denormalize edilir: sayfa daha sonra yeniden adlandirilsa ya
 * da silinse bile Dashboard hangi sayfadan bahsettigini gostermeye devam eder
 * (bkz. RegressionSuiteItem'in pageId'yi SADECE referans olarak tutmasindan
 * FARKLI -- orada ayni oturumda pages zaten yuklu, burada Dashboard HICBIR
 * sayfa listesini cekmez).
 */
@Entity
@Table(name = "test_run_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRunItemEntity {

    @Id
    private String id;

    @Column(name = "test_run_id", nullable = false)
    private String testRunId;

    @Column(name = "page_id", nullable = false)
    private String pageId;

    @Column(name = "page_name", nullable = false)
    private String pageName;

    @Column(name = "endpoint_label", nullable = false)
    private String endpointLabel;

    @Column(name = "verdict", nullable = false)
    private String verdict;

    @Column(name = "detail", length = 500)
    private String detail;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
