package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.ProjectCreateRequest;
import com.rs.gridservice.dto.ProjectResponse;
import com.rs.gridservice.dto.ProjectUpdateRequest;
import com.rs.gridservice.entity.ProjectEntity;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.ProjectRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
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
class JpaProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private TypedQuery<ProjectEntity> typedQuery;

    private JpaProjectServiceImpl service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new JpaProjectServiceImpl(projectRepository, entityManager);
        lenient().when(entityManager.createQuery(anyString(), eq(ProjectEntity.class))).thenReturn(typedQuery);
        lenient().when(typedQuery.setFirstResult(anyInt())).thenReturn(typedQuery);
        lenient().when(typedQuery.setMaxResults(anyInt())).thenReturn(typedQuery);
        lenient().when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
    }

    private ProjectEntity sampleEntity() {
        return ProjectEntity.builder()
                .id("project-1")
                .name("Grid Dashboard")
                .userId("user-1")
                .technology("React")
                .type("web")
                .build();
    }

    @Test
    void addProjectGeneratesIdAndPersists() {
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("Grid Dashboard");
        request.setUserId("user-1");
        request.setTechnology("React");
        request.setType("web");

        ArgumentCaptor<ProjectEntity> captor = ArgumentCaptor.forClass(ProjectEntity.class);
        when(projectRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponse response = service.addProject(request);

        assertThat(response.getId()).isNotBlank();
        assertThat(response.getName()).isEqualTo("Grid Dashboard");
        assertThat(response.getUserId()).isEqualTo("user-1");
        assertThat(response.getTechnology()).isEqualTo("React");
        assertThat(response.getType()).isEqualTo("web");
        assertThat(captor.getValue().getId()).isEqualTo(response.getId());
    }

    @Test
    void getProjectReturnsMappedResponseWhenFound() {
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(sampleEntity()));

        ProjectResponse response = service.getProject("project-1");

        assertThat(response.getId()).isEqualTo("project-1");
        assertThat(response.getName()).isEqualTo("Grid Dashboard");
    }

    @Test
    void getProjectThrowsNotFoundWhenMissing() {
        when(projectRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProject("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllProjectsWithoutSearchSkipsParameterBinding() {
        when(typedQuery.getResultList()).thenReturn(List.of(sampleEntity()));

        List<ProjectResponse> response = service.getAllProjects(0, 50, null);

        assertThat(response).hasSize(1);
        verify(typedQuery, never()).setParameter(anyString(), any());
    }

    @Test
    void getAllProjectsWithBlankSearchSkipsParameterBinding() {
        when(typedQuery.getResultList()).thenReturn(List.of());

        service.getAllProjects(0, 50, "   ");

        verify(typedQuery, never()).setParameter(anyString(), any());
    }

    @Test
    void getAllProjectsWithSearchBindsParameter() {
        when(typedQuery.getResultList()).thenReturn(List.of(sampleEntity()));

        List<ProjectResponse> response = service.getAllProjects(0, 50, "grid");

        assertThat(response).hasSize(1);
        verify(typedQuery, times(1)).setParameter("search", "grid");
    }

    @Test
    void editProjectUpdatesOnlyProvidedFields() {
        ProjectEntity existing = sampleEntity();
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(existing));
        when(projectRepository.save(existing)).thenReturn(existing);

        ProjectUpdateRequest request = new ProjectUpdateRequest();
        request.setTechnology("Kotlin");

        ProjectResponse response = service.editProject("project-1", request);

        assertThat(response.getTechnology()).isEqualTo("Kotlin");
        assertThat(response.getName()).isEqualTo("Grid Dashboard");
        assertThat(response.getUserId()).isEqualTo("user-1");
        assertThat(response.getType()).isEqualTo("web");
    }

    @Test
    void editProjectWithOnlyNameLeavesTechnologyUnset() {
        ProjectEntity existing = sampleEntity();
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(existing));
        when(projectRepository.save(existing)).thenReturn(existing);

        ProjectUpdateRequest request = new ProjectUpdateRequest();
        request.setName("Sadece Isim Degisti");

        ProjectResponse response = service.editProject("project-1", request);

        assertThat(response.getName()).isEqualTo("Sadece Isim Degisti");
        assertThat(response.getTechnology()).isEqualTo("React");
        assertThat(response.getUserId()).isEqualTo("user-1");
        assertThat(response.getType()).isEqualTo("web");
    }

    @Test
    void editProjectWithAllFieldsUpdatesEverything() {
        ProjectEntity existing = sampleEntity();
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(existing));
        when(projectRepository.save(existing)).thenReturn(existing);

        ProjectUpdateRequest request = new ProjectUpdateRequest();
        request.setName("Yeni Isim");
        request.setUserId("user-2");
        request.setTechnology("Vue");
        request.setType("mobile");

        ProjectResponse response = service.editProject("project-1", request);

        assertThat(response.getName()).isEqualTo("Yeni Isim");
        assertThat(response.getUserId()).isEqualTo("user-2");
        assertThat(response.getTechnology()).isEqualTo("Vue");
        assertThat(response.getType()).isEqualTo("mobile");
    }

    @Test
    void editProjectThrowsNotFoundWhenMissing() {
        when(projectRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editProject("missing", new ProjectUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteProjectRemovesWhenExists() {
        when(projectRepository.existsById("project-1")).thenReturn(true);

        service.deleteProject("project-1");

        verify(projectRepository).deleteById("project-1");
    }

    @Test
    void deleteProjectThrowsNotFoundWhenMissing() {
        when(projectRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> service.deleteProject("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(projectRepository, never()).deleteById(anyString());
    }
}
