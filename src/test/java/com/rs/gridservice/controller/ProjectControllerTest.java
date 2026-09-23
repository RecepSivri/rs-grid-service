package com.rs.gridservice.controller;

import com.rs.gridservice.dto.ProjectCreateRequest;
import com.rs.gridservice.dto.ProjectResponse;
import com.rs.gridservice.dto.ProjectUpdateRequest;
import com.rs.gridservice.service.ProjectService;
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
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    @Test
    void addProjectReturnsCreatedWithLocation() {
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("Grid Dashboard");
        request.setUserId("user-1");
        request.setTechnology("React");
        request.setType("web");
        ProjectResponse created = ProjectResponse.builder().id("project-1").name("Grid Dashboard").build();
        when(projectService.addProject(request)).thenReturn(created);

        ResponseEntity<ProjectResponse> response = projectController.addProject(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/v1/projects/project-1");
        assertThat(response.getBody()).isSameAs(created);
    }

    @Test
    void getProjectReturnsProjectFromService() {
        ProjectResponse project = ProjectResponse.builder().id("project-1").build();
        when(projectService.getProject("project-1")).thenReturn(project);

        ResponseEntity<ProjectResponse> response = projectController.getProject("project-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(project);
    }

    @Test
    void getAllProjectsDelegatesUserIdPagingAndSearchToService() {
        List<ProjectResponse> projects = List.of(ProjectResponse.builder().id("project-1").build());
        when(projectService.getAllProjects("user-1", 0, 50, "grid")).thenReturn(projects);

        ResponseEntity<List<ProjectResponse>> response = projectController.getAllProjects("user-1", 0, 50, "grid");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(projects);
    }

    @Test
    void editProjectReturnsUpdatedProject() {
        ProjectUpdateRequest request = new ProjectUpdateRequest();
        request.setTechnology("Kotlin");
        ProjectResponse updated = ProjectResponse.builder().id("project-1").technology("Kotlin").build();
        when(projectService.editProject("project-1", "user-1", request)).thenReturn(updated);

        ResponseEntity<ProjectResponse> response = projectController.editProject("project-1", "user-1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(updated);
    }

    @Test
    void deleteProjectReturnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = projectController.deleteProject("project-1", "user-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(projectService).deleteProject("project-1", "user-1");
    }
}
