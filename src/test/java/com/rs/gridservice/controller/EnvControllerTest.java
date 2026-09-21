package com.rs.gridservice.controller;

import com.rs.gridservice.dto.EnvCreateRequest;
import com.rs.gridservice.dto.EnvResponse;
import com.rs.gridservice.dto.EnvUpdateRequest;
import com.rs.gridservice.service.EnvService;
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
class EnvControllerTest {

    @Mock
    private EnvService envService;

    @InjectMocks
    private EnvController envController;

    @Test
    void addEnvReturnsCreatedWithLocation() {
        EnvCreateRequest request = new EnvCreateRequest();
        request.setName("Local Dev");
        request.setUrl("http://localhost:5500");
        request.setUserId("user-1");
        EnvResponse created = EnvResponse.builder().id("env-1").name("Local Dev")
                .url("http://localhost:5500").userId("user-1").build();
        when(envService.addEnv(request)).thenReturn(created);

        ResponseEntity<EnvResponse> response = envController.addEnv(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/v1/envs/env-1");
        assertThat(response.getBody()).isSameAs(created);
    }

    @Test
    void getEnvReturnsEnvFromService() {
        EnvResponse env = EnvResponse.builder().id("env-1").build();
        when(envService.getEnv("env-1")).thenReturn(env);

        ResponseEntity<EnvResponse> response = envController.getEnv("env-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(env);
    }

    @Test
    void getAllEnvsDelegatesPagingAndSearchToService() {
        List<EnvResponse> envs = List.of(EnvResponse.builder().id("env-1").build());
        when(envService.getAllEnvs(0, 50, "local")).thenReturn(envs);

        ResponseEntity<List<EnvResponse>> response = envController.getAllEnvs(0, 50, "local");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(envs);
    }

    @Test
    void editEnvReturnsUpdatedEnv() {
        EnvUpdateRequest request = new EnvUpdateRequest();
        request.setUrl("http://localhost:6000");
        EnvResponse updated = EnvResponse.builder().id("env-1").url("http://localhost:6000").build();
        when(envService.editEnv("env-1", request)).thenReturn(updated);

        ResponseEntity<EnvResponse> response = envController.editEnv("env-1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(updated);
    }

    @Test
    void deleteEnvReturnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = envController.deleteEnv("env-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(envService).deleteEnv("env-1");
    }
}
