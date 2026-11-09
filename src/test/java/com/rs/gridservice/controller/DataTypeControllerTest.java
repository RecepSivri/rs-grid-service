package com.rs.gridservice.controller;

import com.rs.gridservice.dto.DataTypeCreateRequest;
import com.rs.gridservice.dto.DataTypeResponse;
import com.rs.gridservice.dto.DataTypeUpdateRequest;
import com.rs.gridservice.entity.DataTypeKind;
import com.rs.gridservice.service.DataTypeService;
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
class DataTypeControllerTest {

    @Mock
    private DataTypeService dataTypeService;

    @InjectMocks
    private DataTypeController dataTypeController;

    @Test
    void addDataTypeReturnsCreatedWithLocation() {
        DataTypeCreateRequest request = new DataTypeCreateRequest();
        request.setName("Email");
        request.setKind(DataTypeKind.TEXT);
        request.setPattern("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
        request.setPatternMessage("Gecerli bir e-posta adresi giriniz");
        DataTypeResponse created = DataTypeResponse.builder()
                .id("dt-1").name("Email").kind(DataTypeKind.TEXT)
                .pattern("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").patternMessage("Gecerli bir e-posta adresi giriniz")
                .build();
        when(dataTypeService.addDataType(request)).thenReturn(created);

        ResponseEntity<DataTypeResponse> response = dataTypeController.addDataType(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/v1/data-types/dt-1");
        assertThat(response.getBody()).isSameAs(created);
    }

    @Test
    void getDataTypeReturnsDataTypeFromService() {
        DataTypeResponse dataType = DataTypeResponse.builder().id("dt-1").build();
        when(dataTypeService.getDataType("dt-1")).thenReturn(dataType);

        ResponseEntity<DataTypeResponse> response = dataTypeController.getDataType("dt-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dataType);
    }

    @Test
    void getAllDataTypesDelegatesPagingAndSearchToService() {
        List<DataTypeResponse> dataTypes = List.of(DataTypeResponse.builder().id("dt-1").build());
        when(dataTypeService.getAllDataTypes(0, 50, "em")).thenReturn(dataTypes);

        ResponseEntity<List<DataTypeResponse>> response = dataTypeController.getAllDataTypes(0, 50, "em");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(dataTypes);
    }

    @Test
    void editDataTypeReturnsUpdatedDataType() {
        DataTypeUpdateRequest request = new DataTypeUpdateRequest();
        request.setName("Telefon");
        request.setKind(DataTypeKind.TEXT);
        DataTypeResponse updated = DataTypeResponse.builder().id("dt-1").name("Telefon").kind(DataTypeKind.TEXT).build();
        when(dataTypeService.editDataType("dt-1", request)).thenReturn(updated);

        ResponseEntity<DataTypeResponse> response = dataTypeController.editDataType("dt-1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(updated);
    }

    @Test
    void deleteDataTypeReturnsNoContentAndDelegatesToService() {
        ResponseEntity<Void> response = dataTypeController.deleteDataType("dt-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(dataTypeService).deleteDataType("dt-1");
    }
}
