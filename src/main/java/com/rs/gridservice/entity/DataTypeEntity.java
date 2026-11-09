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

    /** Opsiyonel: bu veri tipinden olusturulan alanlarin degerinin uymasi gereken
     * regex (orn. telefon/TC kimlik/email). Bos/null ise bicim dogrulamasi YAPILMAZ --
     * mevcut TEXT/NUMBER/... davranisi degismeden kalir. Ekran Tasarımı'nda bu
     * veri tipini secen her alan AYNI deseni miras alir (bkz. PageFieldFormField.pattern). */
    @Column(length = 500)
    private String pattern;

    /** pattern eslesmediginde kullaniciya gosterilecek metin (varsayilan/TR) --
     * bos/null ise cagiran taraf (frontend) genel bir "gecersiz deger" mesajina duser. */
    @Column(length = 300)
    private String patternMessage;

    /** patternMessage'in Ingilizce karsiligi -- opsiyonel. Frontend uygulamanin
     * aktif diline gore ikisinden birini secer (bkz. projectPages.controller.ts:
     * resolvePatternMessage); bos/null ise EN modda da patternMessage'e (TR) duser. */
    @Column(length = 300)
    private String patternMessageEn;
}
