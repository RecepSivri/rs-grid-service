package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.PageCreateRequest;
import com.rs.gridservice.dto.PageResponse;
import com.rs.gridservice.dto.PageUpdateRequest;
import com.rs.gridservice.entity.PageEntity;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.PageRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaPageServiceImplTest {

    @Mock
    private PageRepository pageRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private TypedQuery<PageEntity> typedQuery;

    private JpaPageServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JpaPageServiceImpl(pageRepository, entityManager);
        lenient().when(entityManager.createQuery(anyString(), eq(PageEntity.class))).thenReturn(typedQuery);
        lenient().when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        lenient().when(typedQuery.setFirstResult(anyInt())).thenReturn(typedQuery);
        lenient().when(typedQuery.setMaxResults(anyInt())).thenReturn(typedQuery);
    }

    private PageEntity sampleEntity() {
        return PageEntity.builder().id("page-1").projectId("project-1").userId("user-1").name("Kullanicilar")
                .getApi("/api/users/{id}").getAllApi("/api/users").deleteApi("/api/users/{id}")
                .addApi("/api/users").updateApi("/api/users/{id}").batchUpdateApi("/api/users/batch")
                .build();
    }

    @Test
    void addPageGeneratesIdAndPersists() {
        PageCreateRequest request = new PageCreateRequest();
        request.setProjectId("project-1");
        request.setUserId("user-1");
        request.setName("Kullanicilar");
        request.setGetApi("/api/users/{id}");
        request.setGetAllApi("/api/users");
        request.setDeleteApi("/api/users/{id}");
        request.setAddApi("/api/users");
        request.setUpdateApi("/api/users/{id}");
        request.setBatchUpdateApi("/api/users/batch");

        ArgumentCaptor<PageEntity> captor = ArgumentCaptor.forClass(PageEntity.class);
        when(pageRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        PageResponse response = service.addPage(request);

        assertThat(response.getId()).isNotBlank();
        assertThat(response.getProjectId()).isEqualTo("project-1");
        assertThat(response.getUserId()).isEqualTo("user-1");
        assertThat(response.getName()).isEqualTo("Kullanicilar");
        assertThat(response.getGetApi()).isEqualTo("/api/users/{id}");
        assertThat(response.getGetAllApi()).isEqualTo("/api/users");
        assertThat(response.getDeleteApi()).isEqualTo("/api/users/{id}");
        assertThat(response.getAddApi()).isEqualTo("/api/users");
        assertThat(response.getUpdateApi()).isEqualTo("/api/users/{id}");
        assertThat(response.getBatchUpdateApi()).isEqualTo("/api/users/batch");
        assertThat(captor.getValue().getId()).isEqualTo(response.getId());
    }

    @Test
    void getPageReturnsMappedResponseWhenFound() {
        when(pageRepository.findById("page-1")).thenReturn(Optional.of(sampleEntity()));

        PageResponse response = service.getPage("page-1");

        assertThat(response.getProjectId()).isEqualTo("project-1");
        assertThat(response.getUserId()).isEqualTo("user-1");
        assertThat(response.getName()).isEqualTo("Kullanicilar");
    }

    @Test
    void getPageThrowsNotFoundWhenMissing() {
        when(pageRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getPage("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllPagesFiltersByProjectIdAndUserIdWithoutSearch() {
        when(typedQuery.getResultList()).thenReturn(List.of(sampleEntity()));

        List<PageResponse> response = service.getAllPages("project-1", "user-1", 0, 50, null);

        assertThat(response).hasSize(1);
        verify(typedQuery).setParameter("projectId", "project-1");
        verify(typedQuery).setParameter("userId", "user-1");
        verify(typedQuery, never()).setParameter(eq("search"), any());
    }

    @Test
    void getAllPagesWithBlankSearchSkipsSearchParameterBinding() {
        when(typedQuery.getResultList()).thenReturn(List.of());

        service.getAllPages("project-1", "user-1", 0, 50, "   ");

        verify(typedQuery).setParameter("projectId", "project-1");
        verify(typedQuery).setParameter("userId", "user-1");
        verify(typedQuery, never()).setParameter(eq("search"), any());
    }

    @Test
    void getAllPagesWithSearchBindsAllParameters() {
        when(typedQuery.getResultList()).thenReturn(List.of(sampleEntity()));

        List<PageResponse> response = service.getAllPages("project-1", "user-1", 0, 50, "kullan");

        assertThat(response).hasSize(1);
        verify(typedQuery, times(1)).setParameter("projectId", "project-1");
        verify(typedQuery, times(1)).setParameter("userId", "user-1");
        verify(typedQuery, times(1)).setParameter("search", "kullan");
    }

    @Test
    void editPageWithAllFieldsUpdatesEverything() {
        PageEntity existing = sampleEntity();
        when(pageRepository.findById("page-1")).thenReturn(Optional.of(existing));
        when(pageRepository.save(existing)).thenReturn(existing);

        PageUpdateRequest request = new PageUpdateRequest();
        request.setProjectId("project-2");
        request.setUserId("user-2");
        request.setName("Roller");
        request.setGetApi("/api/roles/{id}");
        request.setGetAllApi("/api/roles");
        request.setDeleteApi("/api/roles/{id}");
        request.setAddApi("/api/roles");
        request.setUpdateApi("/api/roles/{id}");
        request.setBatchUpdateApi("/api/roles/batch");

        PageResponse response = service.editPage("page-1", "user-1", request);

        assertThat(response.getProjectId()).isEqualTo("project-2");
        assertThat(response.getUserId()).isEqualTo("user-2");
        assertThat(response.getName()).isEqualTo("Roller");
        assertThat(response.getGetApi()).isEqualTo("/api/roles/{id}");
        assertThat(response.getGetAllApi()).isEqualTo("/api/roles");
        assertThat(response.getDeleteApi()).isEqualTo("/api/roles/{id}");
        assertThat(response.getAddApi()).isEqualTo("/api/roles");
        assertThat(response.getUpdateApi()).isEqualTo("/api/roles/{id}");
        assertThat(response.getBatchUpdateApi()).isEqualTo("/api/roles/batch");
    }

    @Test
    void editPageWithEmptyRequestLeavesAllFieldsUnchanged() {
        PageEntity existing = sampleEntity();
        when(pageRepository.findById("page-1")).thenReturn(Optional.of(existing));
        when(pageRepository.save(existing)).thenReturn(existing);

        PageResponse response = service.editPage("page-1", "user-1", new PageUpdateRequest());

        assertThat(response.getProjectId()).isEqualTo("project-1");
        assertThat(response.getUserId()).isEqualTo("user-1");
        assertThat(response.getName()).isEqualTo("Kullanicilar");
        assertThat(response.getGetApi()).isEqualTo("/api/users/{id}");
        assertThat(response.getGetAllApi()).isEqualTo("/api/users");
        assertThat(response.getDeleteApi()).isEqualTo("/api/users/{id}");
        assertThat(response.getAddApi()).isEqualTo("/api/users");
        assertThat(response.getUpdateApi()).isEqualTo("/api/users/{id}");
        assertThat(response.getBatchUpdateApi()).isEqualTo("/api/users/batch");
    }

    @Test
    void editPageThrowsNotFoundWhenMissing() {
        when(pageRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editPage("missing", "user-1", new PageUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void editPageThrowsNotFoundWhenOwnedByDifferentUser() {
        when(pageRepository.findById("page-1")).thenReturn(Optional.of(sampleEntity()));

        assertThatThrownBy(() -> service.editPage("page-1", "someone-else", new PageUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(pageRepository, never()).save(any());
    }

    @Test
    void deletePageRemovesWhenOwnedByGivenUser() {
        PageEntity existing = sampleEntity();
        when(pageRepository.findById("page-1")).thenReturn(Optional.of(existing));

        service.deletePage("page-1", "user-1");

        verify(pageRepository).delete(existing);
    }

    @Test
    void deletePageThrowsNotFoundWhenMissing() {
        when(pageRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deletePage("missing", "user-1"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(pageRepository, never()).delete(any());
    }

    @Test
    void deletePageThrowsNotFoundWhenOwnedByDifferentUser() {
        when(pageRepository.findById("page-1")).thenReturn(Optional.of(sampleEntity()));

        assertThatThrownBy(() -> service.deletePage("page-1", "someone-else"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(pageRepository, never()).delete(any());
    }
}
