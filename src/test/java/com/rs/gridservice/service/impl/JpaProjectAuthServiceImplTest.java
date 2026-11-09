package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.ProjectAuthResponse;
import com.rs.gridservice.dto.ProjectAuthUpsertRequest;
import com.rs.gridservice.entity.ProjectAuthEntity;
import com.rs.gridservice.entity.ProjectEntity;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.ProjectAuthRepository;
import com.rs.gridservice.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaProjectAuthServiceImplTest {

    @Mock
    private ProjectAuthRepository projectAuthRepository;
    @Mock
    private ProjectRepository projectRepository;

    private JpaProjectAuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JpaProjectAuthServiceImpl(projectAuthRepository, projectRepository);
    }

    private ProjectEntity ownedProject() {
        return ProjectEntity.builder().id("project-1").userId("user-1").name("Demo").technology("React").type("web").build();
    }

    @Test
    void getAuthReturnsEmptyResponseWhenNothingStoredYet() {
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(ownedProject()));
        when(projectAuthRepository.findById("project-1")).thenReturn(Optional.empty());

        ProjectAuthResponse response = service.getAuth("project-1", "user-1");

        assertThat(response.getAccessToken()).isEmpty();
        assertThat(response.getRefreshToken()).isEmpty();
        assertThat(response.getRefreshUrl()).isEmpty();
        assertThat(response.getExpiresAt()).isNull();
    }

    @Test
    void getAuthReturnsStoredValuesWhenPresent() {
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(ownedProject()));
        ProjectAuthEntity stored = ProjectAuthEntity.builder()
                .projectId("project-1").accessToken("tok").refreshToken("rtok")
                .refreshUrl("https://x/refresh").expiresAt(12345L).build();
        when(projectAuthRepository.findById("project-1")).thenReturn(Optional.of(stored));

        ProjectAuthResponse response = service.getAuth("project-1", "user-1");

        assertThat(response.getAccessToken()).isEqualTo("tok");
        assertThat(response.getRefreshToken()).isEqualTo("rtok");
        assertThat(response.getRefreshUrl()).isEqualTo("https://x/refresh");
        assertThat(response.getExpiresAt()).isEqualTo(12345L);
    }

    @Test
    void getAuthThrowsNotFoundWhenProjectMissing() {
        when(projectRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAuth("missing", "user-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAuthThrowsNotFoundWhenProjectOwnedByDifferentUser() {
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(ownedProject()));

        assertThatThrownBy(() -> service.getAuth("project-1", "someone-else"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void saveAuthUpsertsAndReturnsSavedValues() {
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(ownedProject()));
        ArgumentCaptor<ProjectAuthEntity> captor = ArgumentCaptor.forClass(ProjectAuthEntity.class);
        when(projectAuthRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        ProjectAuthUpsertRequest request = new ProjectAuthUpsertRequest();
        request.setAccessToken("tok");
        request.setRefreshToken("rtok");
        request.setRefreshUrl("https://x/refresh");
        request.setExpiresAt(99L);

        ProjectAuthResponse response = service.saveAuth("project-1", "user-1", request);

        assertThat(captor.getValue().getProjectId()).isEqualTo("project-1");
        assertThat(response.getAccessToken()).isEqualTo("tok");
        assertThat(response.getRefreshToken()).isEqualTo("rtok");
        assertThat(response.getRefreshUrl()).isEqualTo("https://x/refresh");
        assertThat(response.getExpiresAt()).isEqualTo(99L);
    }

    @Test
    void saveAuthThrowsNotFoundWhenProjectOwnedByDifferentUser() {
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(ownedProject()));

        assertThatThrownBy(() -> service.saveAuth("project-1", "someone-else", new ProjectAuthUpsertRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(projectAuthRepository, never()).save(any());
    }

    @Test
    void clearAuthDeletesByProjectId() {
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(ownedProject()));

        service.clearAuth("project-1", "user-1");

        verify(projectAuthRepository).deleteById("project-1");
    }

    @Test
    void clearAuthThrowsNotFoundWhenProjectOwnedByDifferentUser() {
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(ownedProject()));

        assertThatThrownBy(() -> service.clearAuth("project-1", "someone-else"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(projectAuthRepository, never()).deleteById(any());
    }
}
