package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.RegressionSuiteItemResponse;
import com.rs.gridservice.dto.RegressionSuiteItemUpsertRequest;
import com.rs.gridservice.entity.ProjectEntity;
import com.rs.gridservice.entity.RegressionSuiteItemEntity;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.ProjectRepository;
import com.rs.gridservice.repository.RegressionSuiteItemRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaRegressionSuiteItemServiceImplTest {

    @Mock
    private RegressionSuiteItemRepository regressionSuiteItemRepository;
    @Mock
    private ProjectRepository projectRepository;

    private JpaRegressionSuiteItemServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JpaRegressionSuiteItemServiceImpl(regressionSuiteItemRepository, projectRepository);
    }

    private ProjectEntity sampleProject() {
        return ProjectEntity.builder().id("project-1").name("Aselsen").userId("user-1").technology("React").type("web").build();
    }

    private RegressionSuiteItemEntity sampleItem(String id, String pageId, String label, int order) {
        return RegressionSuiteItemEntity.builder().id(id).projectId("project-1").pageId(pageId).endpointLabel(label).sortOrder(order).build();
    }

    private RegressionSuiteItemUpsertRequest upsert(String pageId, String label) {
        RegressionSuiteItemUpsertRequest request = new RegressionSuiteItemUpsertRequest();
        request.setPageId(pageId);
        request.setEndpointLabel(label);
        return request;
    }

    @Test
    void getItemsReturnsOrderedMappedResponsesWhenProjectOwnedByUser() {
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(sampleProject()));
        when(regressionSuiteItemRepository.findByProjectIdOrderBySortOrderAsc("project-1"))
                .thenReturn(List.of(sampleItem("item-1", "page-1", "GetAll", 0), sampleItem("item-2", "page-1", "Get", 1)));

        List<RegressionSuiteItemResponse> response = service.getItems("project-1", "user-1");

        assertThat(response).hasSize(2);
        assertThat(response.get(0).getPageId()).isEqualTo("page-1");
        assertThat(response.get(0).getEndpointLabel()).isEqualTo("GetAll");
        assertThat(response.get(0).getSortOrder()).isEqualTo(0);
        assertThat(response.get(1).getEndpointLabel()).isEqualTo("Get");
    }

    @Test
    void getItemsThrowsNotFoundWhenProjectMissing() {
        when(projectRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getItems("missing", "user-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getItemsThrowsNotFoundWhenProjectOwnedByDifferentUser() {
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(sampleProject()));

        assertThatThrownBy(() -> service.getItems("project-1", "someone-else"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void replaceItemsDeletesExistingAndPersistsNewOrderedEntitiesWithFreshIds() {
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(sampleProject()));
        ArgumentCaptor<List<RegressionSuiteItemEntity>> captor = ArgumentCaptor.forClass(List.class);
        when(regressionSuiteItemRepository.saveAll(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        List<RegressionSuiteItemResponse> response = service.replaceItems("project-1", "user-1",
                List.of(upsert("page-1", "GetAll"), upsert("page-1", "Add")));

        verify(regressionSuiteItemRepository).deleteByProjectId("project-1");
        List<RegressionSuiteItemEntity> saved = captor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved.get(0).getId()).isNotBlank();
        assertThat(saved.get(0).getProjectId()).isEqualTo("project-1");
        assertThat(saved.get(0).getPageId()).isEqualTo("page-1");
        assertThat(saved.get(0).getEndpointLabel()).isEqualTo("GetAll");
        assertThat(saved.get(0).getSortOrder()).isEqualTo(0);
        assertThat(saved.get(1).getEndpointLabel()).isEqualTo("Add");
        assertThat(saved.get(1).getSortOrder()).isEqualTo(1);
        assertThat(saved.get(0).getId()).isNotEqualTo(saved.get(1).getId());

        assertThat(response).hasSize(2);
        assertThat(response.get(1).getSortOrder()).isEqualTo(1);
    }

    @Test
    void replaceItemsWithEmptyListClearsTheSuite() {
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(sampleProject()));
        when(regressionSuiteItemRepository.saveAll(anyList())).thenReturn(List.of());

        List<RegressionSuiteItemResponse> response = service.replaceItems("project-1", "user-1", List.of());

        verify(regressionSuiteItemRepository).deleteByProjectId("project-1");
        assertThat(response).isEmpty();
    }

    @Test
    void replaceItemsThrowsNotFoundWhenProjectMissing() {
        when(projectRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.replaceItems("missing", "user-1", List.of()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(regressionSuiteItemRepository, never()).deleteByProjectId(anyString());
        verify(regressionSuiteItemRepository, never()).saveAll(anyList());
    }

    @Test
    void replaceItemsThrowsNotFoundWhenProjectOwnedByDifferentUser() {
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(sampleProject()));

        assertThatThrownBy(() -> service.replaceItems("project-1", "someone-else", List.of()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(regressionSuiteItemRepository, never()).saveAll(anyList());
    }
}
