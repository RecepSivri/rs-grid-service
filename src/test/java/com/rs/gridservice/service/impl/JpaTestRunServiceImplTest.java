package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.TestRunItemResponse;
import com.rs.gridservice.dto.TestRunItemUpsertRequest;
import com.rs.gridservice.dto.TestRunResponse;
import com.rs.gridservice.dto.TestRunUpsertRequest;
import com.rs.gridservice.entity.ProjectEntity;
import com.rs.gridservice.entity.RegressionSuiteItemEntity;
import com.rs.gridservice.entity.TestRunEntity;
import com.rs.gridservice.entity.TestRunItemEntity;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.ProjectRepository;
import com.rs.gridservice.repository.RegressionSuiteItemRepository;
import com.rs.gridservice.repository.TestRunItemRepository;
import com.rs.gridservice.repository.TestRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaTestRunServiceImplTest {

    @Mock
    private TestRunRepository testRunRepository;
    @Mock
    private TestRunItemRepository testRunItemRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private RegressionSuiteItemRepository regressionSuiteItemRepository;

    private JpaTestRunServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JpaTestRunServiceImpl(testRunRepository, testRunItemRepository, projectRepository, regressionSuiteItemRepository);
    }

    private ProjectEntity sampleProject() {
        return ProjectEntity.builder().id("project-1").name("Aselsen").userId("user-1").technology("React").type("web").build();
    }

    private TestRunItemUpsertRequest upsertItem(String pageId, String pageName, String label, String verdict, String detail) {
        TestRunItemUpsertRequest item = new TestRunItemUpsertRequest();
        item.setPageId(pageId);
        item.setPageName(pageName);
        item.setEndpointLabel(label);
        item.setVerdict(verdict);
        item.setDetail(detail);
        return item;
    }

    private TestRunUpsertRequest upsertRequest(List<TestRunItemUpsertRequest> items) {
        TestRunUpsertRequest request = new TestRunUpsertRequest();
        request.setEnvUrl("http://localhost:5080");
        request.setEnvName("test");
        request.setItems(items);
        return request;
    }

    // ---- getRunsForUser ----

    @Test
    void getRunsForUserReturnsEmptyWhenUserHasNoProjects() {
        when(projectRepository.findByUserId("user-1")).thenReturn(List.of());

        assertThat(service.getRunsForUser("user-1")).isEmpty();
        verify(testRunRepository, never()).findByProjectIdIn(anyList());
    }

    @Test
    void getRunsForUserReturnsEmptyWhenNoRunsExistForOwnedProjects() {
        when(projectRepository.findByUserId("user-1")).thenReturn(List.of(sampleProject()));
        when(testRunRepository.findByProjectIdIn(List.of("project-1"))).thenReturn(List.of());

        assertThat(service.getRunsForUser("user-1")).isEmpty();
    }

    @Test
    void getRunsForUserMapsCountsEnvAndOnlyNonPassProblemsInSortOrder() {
        when(projectRepository.findByUserId("user-1")).thenReturn(List.of(sampleProject()));
        TestRunEntity run = TestRunEntity.builder().id("run-1").projectId("project-1").mode("smoke")
                .runAtEpochMs(1000L).envName("test").envUrl("http://x").passCount(2).failCount(1).errorCount(0).skippedCount(1).build();
        when(testRunRepository.findByProjectIdIn(List.of("project-1"))).thenReturn(List.of(run));
        TestRunItemEntity fail = TestRunItemEntity.builder().id("i1").testRunId("run-1").pageId("page-1").pageName("Patients")
                .endpointLabel("GetAll").verdict("fail").detail("Content-Type JSON").sortOrder(0).build();
        TestRunItemEntity skipped = TestRunItemEntity.builder().id("i2").testRunId("run-1").pageId("page-2").pageName("Doctors")
                .endpointLabel("Get").verdict("skipped").detail("Örnek kayıt bulunamadı").sortOrder(1).build();
        when(testRunItemRepository.findByTestRunIdInAndVerdictNotOrderBySortOrderAsc(List.of("run-1"), "pass"))
                .thenReturn(List.of(fail, skipped));

        List<TestRunResponse> responses = service.getRunsForUser("user-1");

        assertThat(responses).hasSize(1);
        TestRunResponse response = responses.get(0);
        assertThat(response.getProjectId()).isEqualTo("project-1");
        assertThat(response.getMode()).isEqualTo("smoke");
        assertThat(response.getEnvName()).isEqualTo("test");
        assertThat(response.getPassCount()).isEqualTo(2);
        assertThat(response.getFailCount()).isEqualTo(1);
        assertThat(response.getSkippedCount()).isEqualTo(1);
        assertThat(response.getProblems()).extracting(TestRunItemResponse::getEndpointLabel).containsExactly("GetAll", "Get");
        assertThat(response.getSuiteChanged()).isNull(); // smoke
    }

    @Test
    void getRunsForUserMarksSuiteChangedWhenCurrentSuiteDiffersFromStoredSignature() {
        when(projectRepository.findByUserId("user-1")).thenReturn(List.of(sampleProject()));
        TestRunEntity run = TestRunEntity.builder().id("run-1").projectId("project-1").mode("regression")
                .runAtEpochMs(1000L).envUrl("http://x").suiteSignature("page-1::GetAll").build();
        when(testRunRepository.findByProjectIdIn(List.of("project-1"))).thenReturn(List.of(run));
        when(testRunItemRepository.findByTestRunIdInAndVerdictNotOrderBySortOrderAsc(List.of("run-1"), "pass")).thenReturn(List.of());
        when(regressionSuiteItemRepository.findByProjectIdOrderBySortOrderAsc("project-1"))
                .thenReturn(List.of(RegressionSuiteItemEntity.builder().id("x").projectId("project-1").pageId("page-1").endpointLabel("Add").sortOrder(0).build()));

        assertThat(service.getRunsForUser("user-1").get(0).getSuiteChanged()).isTrue();
    }

    @Test
    void getRunsForUserMarksSuiteUnchangedWhenCurrentSuiteMatchesStoredSignatureRegardlessOfOrder() {
        when(projectRepository.findByUserId("user-1")).thenReturn(List.of(sampleProject()));
        // Imza siralamadan bagimsiz (sorted) hesaplanir -- kaydedilenle GUNCEL kume
        // ayni elemanlari FARKLI sirada tutsa bile "degisti" sayilmamali.
        TestRunEntity run = TestRunEntity.builder().id("run-1").projectId("project-1").mode("regression")
                .runAtEpochMs(1000L).envUrl("http://x").suiteSignature("page-1::Add|page-1::GetAll").build();
        when(testRunRepository.findByProjectIdIn(List.of("project-1"))).thenReturn(List.of(run));
        when(testRunItemRepository.findByTestRunIdInAndVerdictNotOrderBySortOrderAsc(List.of("run-1"), "pass")).thenReturn(List.of());
        when(regressionSuiteItemRepository.findByProjectIdOrderBySortOrderAsc("project-1")).thenReturn(List.of(
                RegressionSuiteItemEntity.builder().id("a").projectId("project-1").pageId("page-1").endpointLabel("GetAll").sortOrder(0).build(),
                RegressionSuiteItemEntity.builder().id("b").projectId("project-1").pageId("page-1").endpointLabel("Add").sortOrder(1).build()
        ));

        assertThat(service.getRunsForUser("user-1").get(0).getSuiteChanged()).isFalse();
    }

    @Test
    void getRunsForUserLeavesSuiteChangedNullWhenStoredSignatureIsNull() {
        when(projectRepository.findByUserId("user-1")).thenReturn(List.of(sampleProject()));
        TestRunEntity run = TestRunEntity.builder().id("run-1").projectId("project-1").mode("regression")
                .runAtEpochMs(1000L).envUrl("http://x").suiteSignature(null).build();
        when(testRunRepository.findByProjectIdIn(List.of("project-1"))).thenReturn(List.of(run));
        when(testRunItemRepository.findByTestRunIdInAndVerdictNotOrderBySortOrderAsc(List.of("run-1"), "pass")).thenReturn(List.of());

        assertThat(service.getRunsForUser("user-1").get(0).getSuiteChanged()).isNull();
        verify(regressionSuiteItemRepository, never()).findByProjectIdOrderBySortOrderAsc(anyString());
    }

    // ---- replaceRun ----

    @Test
    void replaceRunDeletesPreviousRunAndItemsThenPersistsNewOnesWithFreshIdsAndSequentialOrder() {
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(sampleProject()));
        TestRunEntity previous = TestRunEntity.builder().id("old-run").projectId("project-1").mode("regression").build();
        when(testRunRepository.findByProjectIdAndMode("project-1", "regression")).thenReturn(Optional.of(previous));
        ArgumentCaptor<List<TestRunItemEntity>> captor = ArgumentCaptor.forClass(List.class);
        when(testRunItemRepository.saveAll(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        TestRunResponse response = service.replaceRun("project-1", "regression", "user-1",
                upsertRequest(List.of(
                        upsertItem("page-1", "Patients", "GetAll", "pass", null),
                        upsertItem("page-1", "Patients", "Add", "fail", "Durum kodu 2xx")
                )));

        verify(testRunItemRepository).deleteByTestRunId("old-run");
        verify(testRunRepository).deleteByProjectIdAndMode("project-1", "regression");
        List<TestRunItemEntity> saved = captor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getId()).isNotEqualTo(saved.get(1).getId());
        assertThat(saved.get(0).getSortOrder()).isEqualTo(0);
        assertThat(saved.get(1).getSortOrder()).isEqualTo(1);
        assertThat(response.getPassCount()).isEqualTo(1);
        assertThat(response.getFailCount()).isEqualTo(1);
        assertThat(response.getProblems()).hasSize(1);
        assertThat(response.getProblems().get(0).getEndpointLabel()).isEqualTo("Add");
    }

    @Test
    void replaceRunSkipsItemDeleteWhenNoPreviousRunExisted() {
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(sampleProject()));
        when(testRunRepository.findByProjectIdAndMode("project-1", "smoke")).thenReturn(Optional.empty());
        when(testRunItemRepository.saveAll(anyList())).thenReturn(List.of());

        service.replaceRun("project-1", "smoke", "user-1", upsertRequest(List.of()));

        verify(testRunItemRepository, never()).deleteByTestRunId(anyString());
        verify(testRunRepository).deleteByProjectIdAndMode("project-1", "smoke");
    }

    @Test
    void replaceRunCountsAllFourVerdictsIndependently() {
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(sampleProject()));
        when(testRunRepository.findByProjectIdAndMode("project-1", "regression")).thenReturn(Optional.empty());
        when(testRunItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        TestRunResponse response = service.replaceRun("project-1", "regression", "user-1", upsertRequest(List.of(
                upsertItem("p", "P", "A", "pass", null),
                upsertItem("p", "P", "B", "pass", null),
                upsertItem("p", "P", "C", "fail", "x"),
                upsertItem("p", "P", "D", "error", "y"),
                upsertItem("p", "P", "E", "skipped", "z")
        )));

        assertThat(response.getPassCount()).isEqualTo(2);
        assertThat(response.getFailCount()).isEqualTo(1);
        assertThat(response.getErrorCount()).isEqualTo(1);
        assertThat(response.getSkippedCount()).isEqualTo(1);
    }

    @Test
    void replaceRunSetsSuiteSignatureOnlyForRegressionMode() {
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(sampleProject()));
        when(testRunRepository.findByProjectIdAndMode(anyString(), anyString())).thenReturn(Optional.empty());
        when(testRunItemRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        ArgumentCaptor<TestRunEntity> captor = ArgumentCaptor.forClass(TestRunEntity.class);
        lenient().when(testRunRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.replaceRun("project-1", "smoke", "user-1", upsertRequest(List.of(upsertItem("p", "P", "GetAll", "pass", null))));
        assertThat(captor.getValue().getSuiteSignature()).isNull();

        service.replaceRun("project-1", "regression", "user-1", upsertRequest(List.of(upsertItem("p", "P", "GetAll", "pass", null))));
        assertThat(captor.getValue().getSuiteSignature()).isEqualTo("p::GetAll");
    }

    @Test
    void replaceRunThrowsNotFoundWhenProjectMissing() {
        when(projectRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.replaceRun("missing", "smoke", "user-1", upsertRequest(List.of())))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(testRunItemRepository, never()).saveAll(anyList());
    }

    @Test
    void replaceRunThrowsNotFoundWhenProjectOwnedByDifferentUser() {
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(sampleProject()));

        assertThatThrownBy(() -> service.replaceRun("project-1", "smoke", "someone-else", upsertRequest(List.of())))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(testRunItemRepository, never()).saveAll(anyList());
    }
}
