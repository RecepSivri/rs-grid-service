package com.rs.gridservice.controller;

import com.rs.gridservice.dto.ProjectAuthResponse;
import com.rs.gridservice.dto.ProjectAuthUpsertRequest;
import com.rs.gridservice.service.ProjectAuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectAuthControllerTest {

    @Mock
    private ProjectAuthService projectAuthService;

    @InjectMocks
    private ProjectAuthController projectAuthController;

    @Test
    void getAuthReturnsAuthFromService() {
        ProjectAuthResponse auth = ProjectAuthResponse.builder().accessToken("tok").build();
        when(projectAuthService.getAuth("project-1", "user-1")).thenReturn(auth);

        ResponseEntity<ProjectAuthResponse> response = projectAuthController.getAuth("project-1", "user-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(auth);
    }

    @Test
    void saveAuthReturnsSavedAuth() {
        ProjectAuthUpsertRequest request = new ProjectAuthUpsertRequest();
        request.setAccessToken("tok");
        request.setRefreshToken("rtok");
        request.setRefreshUrl("https://x/refresh");
        ProjectAuthResponse saved = ProjectAuthResponse.builder().accessToken("tok").refreshToken("rtok").build();
        when(projectAuthService.saveAuth("project-1", "user-1", request)).thenReturn(saved);

        ResponseEntity<ProjectAuthResponse> response = projectAuthController.saveAuth("project-1", "user-1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(saved);
    }

    @Test
    void clearAuthReturnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = projectAuthController.clearAuth("project-1", "user-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(projectAuthService).clearAuth("project-1", "user-1");
    }
}
