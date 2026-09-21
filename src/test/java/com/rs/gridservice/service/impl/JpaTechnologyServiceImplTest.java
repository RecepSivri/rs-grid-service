package com.rs.gridservice.service.impl;

import com.rs.gridservice.dto.TechnologyCreateRequest;
import com.rs.gridservice.dto.TechnologyResponse;
import com.rs.gridservice.dto.TechnologyUpdateRequest;
import com.rs.gridservice.entity.TechnologyEntity;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.repository.TechnologyRepository;
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
class JpaTechnologyServiceImplTest {

    @Mock
    private TechnologyRepository technologyRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private TypedQuery<TechnologyEntity> typedQuery;

    private JpaTechnologyServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JpaTechnologyServiceImpl(technologyRepository, entityManager);
        lenient().when(entityManager.createQuery(anyString(), eq(TechnologyEntity.class))).thenReturn(typedQuery);
        lenient().when(typedQuery.setFirstResult(anyInt())).thenReturn(typedQuery);
        lenient().when(typedQuery.setMaxResults(anyInt())).thenReturn(typedQuery);
        lenient().when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
    }

    private TechnologyEntity sampleEntity() {
        return TechnologyEntity.builder().id("tech-1").name("React").build();
    }

    @Test
    void addTechnologyGeneratesIdAndPersists() {
        TechnologyCreateRequest request = new TechnologyCreateRequest();
        request.setName("React");

        ArgumentCaptor<TechnologyEntity> captor = ArgumentCaptor.forClass(TechnologyEntity.class);
        when(technologyRepository.save(captor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        TechnologyResponse response = service.addTechnology(request);

        assertThat(response.getId()).isNotBlank();
        assertThat(response.getName()).isEqualTo("React");
        assertThat(captor.getValue().getId()).isEqualTo(response.getId());
    }

    @Test
    void getTechnologyReturnsMappedResponseWhenFound() {
        when(technologyRepository.findById("tech-1")).thenReturn(Optional.of(sampleEntity()));

        TechnologyResponse response = service.getTechnology("tech-1");

        assertThat(response.getName()).isEqualTo("React");
    }

    @Test
    void getTechnologyThrowsNotFoundWhenMissing() {
        when(technologyRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTechnology("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllTechnologiesWithoutSearchSkipsParameterBinding() {
        when(typedQuery.getResultList()).thenReturn(List.of(sampleEntity()));

        List<TechnologyResponse> response = service.getAllTechnologies(0, 50, null);

        assertThat(response).hasSize(1);
        verify(typedQuery, never()).setParameter(anyString(), any());
    }

    @Test
    void getAllTechnologiesWithBlankSearchSkipsParameterBinding() {
        when(typedQuery.getResultList()).thenReturn(List.of());

        service.getAllTechnologies(0, 50, "   ");

        verify(typedQuery, never()).setParameter(anyString(), any());
    }

    @Test
    void getAllTechnologiesWithSearchBindsParameter() {
        when(typedQuery.getResultList()).thenReturn(List.of(sampleEntity()));

        List<TechnologyResponse> response = service.getAllTechnologies(0, 50, "re");

        assertThat(response).hasSize(1);
        verify(typedQuery, times(1)).setParameter("search", "re");
    }

    @Test
    void editTechnologyUpdatesName() {
        TechnologyEntity existing = sampleEntity();
        when(technologyRepository.findById("tech-1")).thenReturn(Optional.of(existing));
        when(technologyRepository.save(existing)).thenReturn(existing);

        TechnologyUpdateRequest request = new TechnologyUpdateRequest();
        request.setName("Vue.js");

        TechnologyResponse response = service.editTechnology("tech-1", request);

        assertThat(response.getName()).isEqualTo("Vue.js");
    }

    @Test
    void editTechnologyThrowsNotFoundWhenMissing() {
        when(technologyRepository.findById("missing")).thenReturn(Optional.empty());

        TechnologyUpdateRequest request = new TechnologyUpdateRequest();
        request.setName("Vue.js");

        assertThatThrownBy(() -> service.editTechnology("missing", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteTechnologyRemovesWhenExists() {
        when(technologyRepository.existsById("tech-1")).thenReturn(true);

        service.deleteTechnology("tech-1");

        verify(technologyRepository).deleteById("tech-1");
    }

    @Test
    void deleteTechnologyThrowsNotFoundWhenMissing() {
        when(technologyRepository.existsById("missing")).thenReturn(false);

        assertThatThrownBy(() -> service.deleteTechnology("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(technologyRepository, never()).deleteById(anyString());
    }
}
