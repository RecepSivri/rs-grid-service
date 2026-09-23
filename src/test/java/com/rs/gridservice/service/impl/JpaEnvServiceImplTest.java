package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.EnvCreateRequest;
import com.rs.gridservice.dto.EnvResponse;
import com.rs.gridservice.dto.EnvUpdateRequest;
import com.rs.gridservice.entity.EnvEntity;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.EnvRepository;
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
class JpaEnvServiceImplTest {

    @Mock
    private EnvRepository envRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private TypedQuery<EnvEntity> typedQuery;

    private JpaEnvServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JpaEnvServiceImpl(envRepository, entityManager);
        lenient().when(entityManager.createQuery(anyString(), eq(EnvEntity.class))).thenReturn(typedQuery);
        lenient().when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        lenient().when(typedQuery.setFirstResult(anyInt())).thenReturn(typedQuery);
        lenient().when(typedQuery.setMaxResults(anyInt())).thenReturn(typedQuery);
    }

    private EnvEntity sampleEntity() {
        return EnvEntity.builder().id("env-1").name("Local Dev").url("http://localhost:5500").userId("user-1").build();
    }

    @Test
    void addEnvGeneratesIdAndPersists() {
        EnvCreateRequest request = new EnvCreateRequest();
        request.setName("Local Dev");
        request.setUrl("http://localhost:5500");
        request.setUserId("user-1");

        ArgumentCaptor<EnvEntity> captor = ArgumentCaptor.forClass(EnvEntity.class);
        when(envRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        EnvResponse response = service.addEnv(request);

        assertThat(response.getId()).isNotBlank();
        assertThat(response.getName()).isEqualTo("Local Dev");
        assertThat(response.getUrl()).isEqualTo("http://localhost:5500");
        assertThat(response.getUserId()).isEqualTo("user-1");
        assertThat(captor.getValue().getId()).isEqualTo(response.getId());
    }

    @Test
    void getEnvReturnsMappedResponseWhenFound() {
        when(envRepository.findById("env-1")).thenReturn(Optional.of(sampleEntity()));

        EnvResponse response = service.getEnv("env-1");

        assertThat(response.getName()).isEqualTo("Local Dev");
        assertThat(response.getUrl()).isEqualTo("http://localhost:5500");
        assertThat(response.getUserId()).isEqualTo("user-1");
    }

    @Test
    void getEnvThrowsNotFoundWhenMissing() {
        when(envRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEnv("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllEnvsFiltersByUserIdWithoutSearch() {
        when(typedQuery.getResultList()).thenReturn(List.of(sampleEntity()));

        List<EnvResponse> response = service.getAllEnvs("user-1", 0, 50, null);

        assertThat(response).hasSize(1);
        verify(typedQuery).setParameter("userId", "user-1");
        verify(typedQuery, never()).setParameter(eq("search"), any());
    }

    @Test
    void getAllEnvsWithBlankSearchSkipsSearchParameterBinding() {
        when(typedQuery.getResultList()).thenReturn(List.of());

        service.getAllEnvs("user-1", 0, 50, "   ");

        verify(typedQuery).setParameter("userId", "user-1");
        verify(typedQuery, never()).setParameter(eq("search"), any());
    }

    @Test
    void getAllEnvsWithSearchBindsBothParameters() {
        when(typedQuery.getResultList()).thenReturn(List.of(sampleEntity()));

        List<EnvResponse> response = service.getAllEnvs("user-1", 0, 50, "local");

        assertThat(response).hasSize(1);
        verify(typedQuery, times(1)).setParameter("userId", "user-1");
        verify(typedQuery, times(1)).setParameter("search", "local");
    }

    @Test
    void editEnvWithOnlyNameLeavesOthersUnset() {
        EnvEntity existing = sampleEntity();
        when(envRepository.findById("env-1")).thenReturn(Optional.of(existing));
        when(envRepository.save(existing)).thenReturn(existing);

        EnvUpdateRequest request = new EnvUpdateRequest();
        request.setName("Local Dev v2");

        EnvResponse response = service.editEnv("env-1", "user-1", request);

        assertThat(response.getName()).isEqualTo("Local Dev v2");
        assertThat(response.getUrl()).isEqualTo("http://localhost:5500");
        assertThat(response.getUserId()).isEqualTo("user-1");
    }

    @Test
    void editEnvWithOnlyUrlLeavesOthersUnset() {
        EnvEntity existing = sampleEntity();
        when(envRepository.findById("env-1")).thenReturn(Optional.of(existing));
        when(envRepository.save(existing)).thenReturn(existing);

        EnvUpdateRequest request = new EnvUpdateRequest();
        request.setUrl("http://localhost:6000");

        EnvResponse response = service.editEnv("env-1", "user-1", request);

        assertThat(response.getUrl()).isEqualTo("http://localhost:6000");
        assertThat(response.getName()).isEqualTo("Local Dev");
        assertThat(response.getUserId()).isEqualTo("user-1");
    }

    @Test
    void editEnvWithOnlyUserIdTransfersOwnership() {
        EnvEntity existing = sampleEntity();
        when(envRepository.findById("env-1")).thenReturn(Optional.of(existing));
        when(envRepository.save(existing)).thenReturn(existing);

        EnvUpdateRequest request = new EnvUpdateRequest();
        request.setUserId("user-2");

        EnvResponse response = service.editEnv("env-1", "user-1", request);

        assertThat(response.getUserId()).isEqualTo("user-2");
        assertThat(response.getName()).isEqualTo("Local Dev");
        assertThat(response.getUrl()).isEqualTo("http://localhost:5500");
    }

    @Test
    void editEnvWithAllFieldsUpdatesEverything() {
        EnvEntity existing = sampleEntity();
        when(envRepository.findById("env-1")).thenReturn(Optional.of(existing));
        when(envRepository.save(existing)).thenReturn(existing);

        EnvUpdateRequest request = new EnvUpdateRequest();
        request.setName("Prod");
        request.setUrl("http://localhost:7000");
        request.setUserId("user-2");

        EnvResponse response = service.editEnv("env-1", "user-1", request);

        assertThat(response.getName()).isEqualTo("Prod");
        assertThat(response.getUrl()).isEqualTo("http://localhost:7000");
        assertThat(response.getUserId()).isEqualTo("user-2");
    }

    @Test
    void editEnvThrowsNotFoundWhenMissing() {
        when(envRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.editEnv("missing", "user-1", new EnvUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void editEnvThrowsNotFoundWhenOwnedByDifferentUser() {
        when(envRepository.findById("env-1")).thenReturn(Optional.of(sampleEntity()));

        assertThatThrownBy(() -> service.editEnv("env-1", "someone-else", new EnvUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(envRepository, never()).save(any());
    }

    @Test
    void deleteEnvRemovesWhenOwnedByGivenUser() {
        EnvEntity existing = sampleEntity();
        when(envRepository.findById("env-1")).thenReturn(Optional.of(existing));

        service.deleteEnv("env-1", "user-1");

        verify(envRepository).delete(existing);
    }

    @Test
    void deleteEnvThrowsNotFoundWhenMissing() {
        when(envRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteEnv("missing", "user-1"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(envRepository, never()).delete(any());
    }

    @Test
    void deleteEnvThrowsNotFoundWhenOwnedByDifferentUser() {
        when(envRepository.findById("env-1")).thenReturn(Optional.of(sampleEntity()));

        assertThatThrownBy(() -> service.deleteEnv("env-1", "someone-else"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(envRepository, never()).delete(any());
    }
}
