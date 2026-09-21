package com.rs.gridservice.controller;

import com.rs.gridservice.dto.TechnologyCreateRequest;
import com.rs.gridservice.dto.TechnologyResponse;
import com.rs.gridservice.dto.TechnologyUpdateRequest;
import com.rs.gridservice.service.TechnologyService;
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
class TechnologyControllerTest {

    @Mock
    private TechnologyService technologyService;

    @InjectMocks
    private TechnologyController technologyController;

    @Test
    void addTechnologyReturnsCreatedWithLocation() {
        TechnologyCreateRequest request = new TechnologyCreateRequest();
        request.setName("React");
        TechnologyResponse created = TechnologyResponse.builder().id("tech-1").name("React").build();
        when(technologyService.addTechnology(request)).thenReturn(created);

        ResponseEntity<TechnologyResponse> response = technologyController.addTechnology(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/v1/technologies/tech-1");
        assertThat(response.getBody()).isSameAs(created);
    }

    @Test
    void getTechnologyReturnsTechnologyFromService() {
        TechnologyResponse technology = TechnologyResponse.builder().id("tech-1").build();
        when(technologyService.getTechnology("tech-1")).thenReturn(technology);

        ResponseEntity<TechnologyResponse> response = technologyController.getTechnology("tech-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(technology);
    }

    @Test
    void getAllTechnologiesDelegatesPagingAndSearchToService() {
        List<TechnologyResponse> technologies = List.of(TechnologyResponse.builder().id("tech-1").build());
        when(technologyService.getAllTechnologies(0, 50, "re")).thenReturn(technologies);

        ResponseEntity<List<TechnologyResponse>> response = technologyController.getAllTechnologies(0, 50, "re");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(technologies);
    }

    @Test
    void editTechnologyReturnsUpdatedTechnology() {
        TechnologyUpdateRequest request = new TechnologyUpdateRequest();
        request.setName("Vue.js");
        TechnologyResponse updated = TechnologyResponse.builder().id("tech-1").name("Vue.js").build();
        when(technologyService.editTechnology("tech-1", request)).thenReturn(updated);

        ResponseEntity<TechnologyResponse> response = technologyController.editTechnology("tech-1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(updated);
    }

    @Test
    void deleteTechnologyReturnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = technologyController.deleteTechnology("tech-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(technologyService).deleteTechnology("tech-1");
    }
}
