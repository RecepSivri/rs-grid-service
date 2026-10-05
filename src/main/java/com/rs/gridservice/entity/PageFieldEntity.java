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
 * "page_field" tablosuna karsilik gelen JPA entity'si. Bir Page'in (Sayfa) tek
 * bir alaninin nasil render edilecegini (inputType/visible/readonly/dataType)
 * ve dataType "Lookup" ise hangi baska Page'e/nasil cozumlenecegini tutar.
 * pageId/dataTypeId/lookupTargetPageId hepsi gevsek (duz String) referanslar --
 * bu kodebase'te hicbir yerde JPA iliskisi (@ManyToOne vb.) kullanilmiyor,
 * bkz. ProjectEntity.technology/type ayni desen.
 */
@Entity
@Table(name = "page_field")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageFieldEntity {

    @Id
    private String id;

    @Column(name = "page_id", nullable = false)
    private String pageId;

    @Column(name = "data_field", nullable = false)
    private String dataField;

    private String caption;

    @Column(name = "input_type", nullable = false)
    private String inputType;

    @Column(nullable = false)
    private boolean visible;

    @Column(nullable = false)
    private boolean readonly;

    @Column(name = "data_type_id")
    private String dataTypeId;

    @Column(name = "lookup_target_page_id")
    private String lookupTargetPageId;

    @Column(name = "lookup_value_field")
    private String lookupValueField;

    @Column(name = "lookup_template")
    private String lookupTemplate;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    // Kasitli nullable + kutulu Boolean (visible/readonly gibi primitive+NOT NULL degil):
    // bu kolon eklendiginde tabloda zaten satir vardi, ddl-auto=update bunlara NULL yazar --
    // primitive boolean bunu okurken NPE atardi.
    @Column(name = "required")
    private Boolean required;
}
