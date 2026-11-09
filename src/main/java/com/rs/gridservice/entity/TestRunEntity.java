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
 * "test_run" tablosuna karsilik gelen JPA entity'si -- bir projenin TEK bir
 * modunun (Smoke/Regresyon) EN SON "Tümünü Çalıştır" sonucunun ozeti (bkz.
 * Dashboard'daki "Test Durumu" paneli). Proje basina mod basina TEK satir
 * olur -- her yeni calistirma bir onceki satiri SILIP YENIDEN yazar
 * (JpaTestRunServiceImpl.replaceRun, PageField/RegressionSuiteItem ile AYNI
 * "liste" deseni, burada liste = testRunItem cocuklari). Tekil satir
 * calistirmalari (handleRunRow) burayi HIC GUNCELLEMEZ -- sadece "Tümünü
 * Çalıştır" bir anlik goruntu uretir, yoksa panel yarisi guncel yarisi gunler
 * once kalan tutarsiz bir "GEÇTİ" gosterebilirdi.
 */
@Entity
@Table(name = "test_run")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRunEntity {

    @Id
    private String id;

    @Column(name = "project_id", nullable = false)
    private String projectId;

    @Column(name = "mode", nullable = false)
    private String mode;

    // Instant DEGIL: bu entity katmaninda hic temporal kolon emsali yok, testler
    // H2'de prod Postgres'te calisiyor ve ddl-auto=update ilk dialect
    // uyusmazligini calisma zamaninda gosterir. epoch ms hem JPA'da hem
    // Jackson'da hem frontend'de (new Date(ms)) belirsizlik tasimaz.
    @Column(name = "run_at_epoch_ms", nullable = false)
    private long runAtEpochMs;

    @Column(name = "env_name")
    private String envName;

    @Column(name = "env_url", nullable = false)
    private String envUrl;

    @Column(name = "pass_count", nullable = false)
    private int passCount;

    @Column(name = "fail_count", nullable = false)
    private int failCount;

    @Column(name = "error_count", nullable = false)
    private int errorCount;

    @Column(name = "skipped_count", nullable = false)
    private int skippedCount;

    // SADECE "regression" modunda doldurulur (Smoke'ta NULL): calistirma aninda
    // kosulan satirlarin "pageId::label" kumesinin SIRALANMIS/birlestirilmis hali.
    // Dashboard bunu su anki RegressionSuiteItem kumesiyle karsilastirip suite
    // calistirmadan SONRA degisti mi (bkz. TestRunResponse.suiteChanged) anlar --
    // tum satirlari tekrar cekmek yerine (buyuk/hepsi-yesil calistirmalarda
    // Dashboard'un toplu okumasini agirlastirirdi) TEK bir String karsilastirmasi.
    @Column(name = "suite_signature", length = 4000)
    private String suiteSignature;
}
