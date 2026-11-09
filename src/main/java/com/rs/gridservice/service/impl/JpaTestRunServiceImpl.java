package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.TestRunItemResponse;
import com.rs.gridservice.dto.TestRunItemUpsertRequest;
import com.rs.gridservice.dto.TestRunResponse;
import com.rs.gridservice.dto.TestRunUpsertRequest;
import com.rs.gridservice.entity.ProjectEntity;
import com.rs.gridservice.entity.TestRunEntity;
import com.rs.gridservice.entity.TestRunItemEntity;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.ProjectRepository;
import com.rs.gridservice.repository.RegressionSuiteItemRepository;
import com.rs.gridservice.repository.TestRunItemRepository;
import com.rs.gridservice.repository.TestRunRepository;
import com.rs.gridservice.service.TestRunService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * "Tümünü Çalıştır" tamamlandiginda bir projenin TEK bir modunun (Smoke/
 * Regresyon) anlik goruntusunu kendi veritabanimizdaki ("test_run" +
 * "test_run_item" tablolari) verilere karsi gerceklestiren servis
 * implementasyonu. Tekil satir CRUD'u yok -- PageField/RegressionSuiteItem ile
 * AYNI "liste" deseni: replaceRun() ilgili proje+modun butun satirlarini silip
 * yeniden yazar.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JpaTestRunServiceImpl implements TestRunService {

    private static final String PASS = "pass";

    private final TestRunRepository testRunRepository;
    private final TestRunItemRepository testRunItemRepository;
    private final ProjectRepository projectRepository;
    private final RegressionSuiteItemRepository regressionSuiteItemRepository;

    @Override
    public List<TestRunResponse> getRunsForUser(String userId) {
        List<String> projectIds = projectRepository.findByUserId(userId).stream().map(ProjectEntity::getId).toList();
        if (projectIds.isEmpty()) {
            return List.of();
        }

        List<TestRunEntity> runs = testRunRepository.findByProjectIdIn(projectIds);
        if (runs.isEmpty()) {
            return List.of();
        }

        List<String> runIds = runs.stream().map(TestRunEntity::getId).toList();
        Map<String, List<TestRunItemEntity>> problemsByRunId = testRunItemRepository
                .findByTestRunIdInAndVerdictNotOrderBySortOrderAsc(runIds, PASS).stream()
                .collect(Collectors.groupingBy(TestRunItemEntity::getTestRunId));

        return runs.stream().map(run -> toResponse(run, problemsByRunId.getOrDefault(run.getId(), List.of()))).toList();
    }

    @Override
    @Transactional
    public TestRunResponse replaceRun(String projectId, String mode, String userId, TestRunUpsertRequest request) {
        findOwnedProject(projectId, userId);

        testRunRepository.findByProjectIdAndMode(projectId, mode)
                .ifPresent(old -> testRunItemRepository.deleteByTestRunId(old.getId()));
        testRunRepository.deleteByProjectIdAndMode(projectId, mode);

        List<TestRunItemUpsertRequest> requestItems = request.getItems();
        int passCount = countVerdict(requestItems, "pass");
        int failCount = countVerdict(requestItems, "fail");
        int errorCount = countVerdict(requestItems, "error");
        int skippedCount = countVerdict(requestItems, "skipped");

        String runId = UUID.randomUUID().toString();
        TestRunEntity run = TestRunEntity.builder()
                .id(runId)
                .projectId(projectId)
                .mode(mode)
                .runAtEpochMs(System.currentTimeMillis())
                .envName(request.getEnvName())
                .envUrl(request.getEnvUrl())
                .passCount(passCount)
                .failCount(failCount)
                .errorCount(errorCount)
                .skippedCount(skippedCount)
                .suiteSignature("regression".equals(mode) ? computeSuiteSignature(requestItems.stream().map(this::itemKey).toList()) : null)
                .build();
        testRunRepository.save(run);

        List<TestRunItemEntity> itemEntities = new ArrayList<>();
        int order = 0;
        for (TestRunItemUpsertRequest item : requestItems) {
            itemEntities.add(TestRunItemEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .testRunId(runId)
                    .pageId(item.getPageId())
                    .pageName(item.getPageName())
                    .endpointLabel(item.getEndpointLabel())
                    .verdict(item.getVerdict())
                    .detail(item.getDetail())
                    .sortOrder(order++)
                    .build());
        }
        testRunItemRepository.saveAll(itemEntities);
        log.info("Test calistirma anlik goruntusu kaydedildi: projectId={}, mode={}, gecti={}, basarisiz={}, hata={}, atlandi={}",
                projectId, mode, passCount, failCount, errorCount, skippedCount);

        List<TestRunItemEntity> problems = itemEntities.stream().filter(i -> !PASS.equals(i.getVerdict())).toList();
        return toResponse(run, problems);
    }

    private int countVerdict(List<TestRunItemUpsertRequest> items, String verdict) {
        return (int) items.stream().filter(i -> verdict.equals(i.getVerdict())).count();
    }

    private String itemKey(TestRunItemUpsertRequest item) {
        return item.getPageId() + "::" + item.getEndpointLabel();
    }

    /** Siralamadan BAGIMSIZ bir imza -- suite'i SADECE yeniden siralamak (uyelik
     * ayni kalirken) "degisti" olarak yanlis isaretlenmesin diye once sirala. */
    private String computeSuiteSignature(List<String> keys) {
        return keys.stream().sorted().collect(Collectors.joining("|"));
    }

    /** Kaydi bulur ve gercekten userId'ye ait oldugunu dogrular; degilse "bulunamadi" ile aynen davranir. */
    private ProjectEntity findOwnedProject(String projectId, String userId) {
        return projectRepository.findById(projectId)
                .filter(entity -> entity.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Proje bulunamadi: id=" + projectId));
    }

    private TestRunResponse toResponse(TestRunEntity run, List<TestRunItemEntity> problems) {
        Boolean suiteChanged = null;
        if ("regression".equals(run.getMode()) && run.getSuiteSignature() != null) {
            String currentSignature = computeSuiteSignature(
                    regressionSuiteItemRepository.findByProjectIdOrderBySortOrderAsc(run.getProjectId()).stream()
                            .map(i -> i.getPageId() + "::" + i.getEndpointLabel())
                            .toList());
            suiteChanged = !run.getSuiteSignature().equals(currentSignature);
        }
        return TestRunResponse.builder()
                .id(run.getId())
                .projectId(run.getProjectId())
                .mode(run.getMode())
                .runAtEpochMs(run.getRunAtEpochMs())
                .envName(run.getEnvName())
                .envUrl(run.getEnvUrl())
                .passCount(run.getPassCount())
                .failCount(run.getFailCount())
                .errorCount(run.getErrorCount())
                .skippedCount(run.getSkippedCount())
                .suiteChanged(suiteChanged)
                .problems(problems.stream().map(this::toItemResponse).toList())
                .build();
    }

    private TestRunItemResponse toItemResponse(TestRunItemEntity entity) {
        return TestRunItemResponse.builder()
                .pageId(entity.getPageId())
                .pageName(entity.getPageName())
                .endpointLabel(entity.getEndpointLabel())
                .verdict(entity.getVerdict())
                .detail(entity.getDetail())
                .sortOrder(entity.getSortOrder())
                .build();
    }
}
