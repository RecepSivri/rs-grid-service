package com.rs.gridservice.controller;

import com.rs.gridservice.dto.ProjectTypeCreateRequest;
import com.rs.gridservice.dto.ProjectTypeResponse;
import com.rs.gridservice.dto.ProjectTypeUpdateRequest;
import com.rs.gridservice.service.ProjectTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectTypeControllerTest {

    @Mock
    private ProjectTypeService projectTypeService;

    @InjectMocks
    private ProjectTypeController projectTypeController;

    @Test
    void addProjectTypeReturnsCreatedWithLocation() {
        ProjectTypeCreateRequest request = new ProjectTypeCreateRequest();
        request.setName("web");
        ProjectTypeResponse created = ProjectTypeResponse.builder().id("type-1").name("web").build();
        when(projectTypeService.addProjectType(request)).thenReturn(created);

        ResponseEntity<ProjectTypeResponse> response = projectTypeController.addProjectType(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/v1/project-types/type-1");
        assertThat(response.getBody()).isSameAs(created);
    }

    @Test
    void getProjectTypeReturnsProjectTypeFromService() {
        ProjectTypeResponse type = ProjectTypeResponse.builder().id("type-1").build();
        when(projectTypeService.getProjectType("type-1")).thenReturn(type);

        ResponseEntity<ProjectTypeResponse> response = projectTypeController.getProjectType("type-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(type);
    }

    @Test
    void getAllProjectTypesDelegatesPagingAndSearchToService() {
        List<ProjectTypeResponse> types = List.of(ProjectTypeResponse.builder().id("type-1").build());
        when(projectTypeService.getAllProjectTypes(0, 50, "we")).thenReturn(types);

        ResponseEntity<List<ProjectTypeResponse>> response = projectTypeController.getAllProjectTypes(0, 50, "we");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(types);
    }

    @Test
    void editProjectTypeReturnsUpdatedProjectType() {
        ProjectTypeUpdateRequest request = new ProjectTypeUpdateRequest();
        request.setName("desktop");
        ProjectTypeResponse updated = ProjectTypeResponse.builder().id("type-1").name("desktop").build();
        when(projectTypeService.editProjectType("type-1", request)).thenReturn(updated);

        ResponseEntity<ProjectTypeResponse> response = projectTypeController.editProjectType("type-1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(updated);
    }

    @Test
    void deleteProjectTypeReturnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = projectTypeController.deleteProjectType("type-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(projectTypeService).deleteProjectType("type-1");
    }
}
