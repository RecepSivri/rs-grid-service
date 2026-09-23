package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.ProjectTypeCreateRequest;
import com.rs.gridservice.dto.ProjectTypeResponse;
import com.rs.gridservice.dto.ProjectTypeUpdateRequest;
import com.rs.gridservice.entity.ProjectTypeEntity;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.ProjectRepository;
import com.rs.gridservice.repository.ProjectTypeRepository;
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
class JpaProjectTypeServiceImplTest {

    @Mock
    private ProjectTypeRepository projectTypeRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private TypedQuery<ProjectTypeEntity> typedQuery;

    private JpaProjectTypeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JpaProjectTypeServiceImpl(projectTypeRepository, projectRepository, entityManager);
        lenient().when(entityManager.createQuery(anyString(), eq(ProjectTypeEntity.class))).thenReturn(typedQuery);
        lenient().when(typedQuery.setFirstResult(anyInt())).thenReturn(typedQuery);
        lenient().when(typedQuery.setMaxResults(anyInt())).thenReturn(typedQuery);
        lenient().when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
    }

    private ProjectTypeEntity sampleEntity() {
        return ProjectTypeEntity.builder().id("type-1").name("web").build();
    }

    @Test
    void addProjectTypeGeneratesIdAndPersists() {
        ProjectTypeCreateRequest request = new ProjectTypeCreateRequest();
        request.setName("web");

        ArgumentCaptor<ProjectTypeEntity> captor = ArgumentCaptor.forClass(ProjectTypeEntity.class);
        when(projectTypeRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectTypeResponse response = service.addProjectType(request);

        assertThat(response.getId()).isNotBlank();
        assertThat(response.getName()).isEqualTo("web");
        assertThat(captor.getValue().getId()).isEqualTo(response.getId());
    }

    @Test
    void getProjectTypeReturnsMappedResponseWhenFound() {
        when(projectTypeRepository.findById("type-1")).thenReturn(Optional.of(sampleEntity()));

        ProjectTypeResponse response = service.getProjectType("type-1");

        assertThat(response.getName()).isEqualTo("web");
    }

    @Test
    void getProjectTypeThrowsNotFoundWhenMissing() {
        when(projectTypeRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getProjectType("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllProjectTypesWithoutSearchSkipsParameterBinding() {
        when(typedQuery.getResultList()).thenReturn(List.of(sampleEntity()));

        List<ProjectTypeResponse> response = service.getAllProjectTypes(0, 50, null);

        assertThat(response).hasSize(1);
        verify(typedQuery, never()).setParameter(anyString(), any());
    }

    @Test
    void getAllProjectTypesWithBlankSearchSkipsParameterBinding() {
        when(typedQuery.getResultList()).thenReturn(List.of());

        service.getAllProjectTypes(0, 50, "   ");

        verify(typedQuery, never()).setParameter(anyString(), any());
    }

    @Test
    void getAllProjectTypesWithSearchBindsParameter() {
        when(typedQuery.getResultList()).thenReturn(List.of(sampleEntity()));

        List<ProjectTypeResponse> response = service.getAllProjectTypes(0, 50, "we");

        assertThat(response).hasSize(1);
        verify(typedQuery, times(1)).setParameter("search", "we");
    }

    @Test
    void editProjectTypeUpdatesName() {
        ProjectTypeEntity existing = sampleEntity();
        when(projectTypeRepository.findById("type-1")).thenReturn(Optional.of(existing));
        when(projectTypeRepository.save(existing)).thenReturn(existing);

        ProjectTypeUpdateRequest request = new ProjectTypeUpdateRequest();
        request.setName("desktop");

        ProjectTypeResponse response = service.editProjectType("type-1", request);

        assertThat(response.getName()).isEqualTo("desktop");
    }

    @Test
    void editProjectTypeWithChangedNameCascadesRenameToExistingProjects() {
        ProjectTypeEntity existing = sampleEntity(); // name = "web"
        when(projectTypeRepository.findById("type-1")).thenReturn(Optional.of(existing));
        when(projectTypeRepository.save(existing)).thenReturn(existing);

        ProjectTypeUpdateRequest request = new ProjectTypeUpdateRequest();
        request.setName("Web");

        service.editProjectType("type-1", request);

        // Project.type bir FK degil, isim kopyasi -- rename olunca zaten bu
        // ismi kullanan projelerin de guncellenmesi gerekir.
        verify(projectRepository).renameTypeReferences("web", "Web");
    }

    @Test
    void editProjectTypeWithUnchangedNameDoesNotCascadeRename() {
        ProjectTypeEntity existing = sampleEntity(); // name = "web"
        when(projectTypeRepository.findById("type-1")).thenReturn(Optional.of(existing));
        when(projectTypeRepository.save(existing)).thenReturn(existing);

        ProjectTypeUpdateRequest request = new ProjectTypeUpdateRequest();
        request.setName("web");

        service.editProjectType("type-1", request);

        verify(projectRepository, never()).renameTypeReferences(anyString(), anyString());
    }

    @Test
    void editProjectTypeThrowsNotFoundWhenMissing() {
        when(projectTypeRepository.findById("missing")).thenReturn(Optional.empty());

        ProjectTypeUpdateRequest request = new ProjectTypeUpdateRequest();
        request.setName("desktop");

        assertThatThrownBy(() -> service.editProjectType("missing", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteProjectTypeRemovesWhenExists() {
        when(projectTypeRepository.existsById("type-1")).thenReturn(true);

        service.deleteProjectType("type-1");

        verify(projectTypeRepository).deleteById("type-1");
    }

    @Test
    void deleteProjectTypeThrowsNotFoundWhenMissing() {
        when(projectTypeRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> service.deleteProjectType("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(projectTypeRepository, never()).deleteById(anyString());
    }
}
