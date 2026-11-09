package com.rs.gridservice.controller;

import com.rs.gridservice.dto.PageFieldResponse;
import com.rs.gridservice.dto.PageFieldUpsertRequest;
import com.rs.gridservice.service.PageFieldService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PageFieldControllerTest {

    @Mock
    private PageFieldService pageFieldService;

    @InjectMocks
    private PageFieldController pageFieldController;

    @Test
    void getFieldsReturnsFieldsFromService() {
        List<PageFieldResponse> fields = List.of(PageFieldResponse.builder().id("field-1").pageId("page-1").dataField("name").build());
        when(pageFieldService.getFields("page-1", "user-1")).thenReturn(fields);

        ResponseEntity<List<PageFieldResponse>> response = pageFieldController.getFields("page-1", "user-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(fields);
    }

    @Test
    void replaceFieldsDelegatesToServiceAndReturnsSavedList() {
        PageFieldUpsertRequest request = new PageFieldUpsertRequest();
        request.setDataField("name");
        request.setInputType("text");
        request.setVisible(true);
        request.setReadonly(false);
        request.setRequired(true);
        List<PageFieldResponse> saved = List.of(PageFieldResponse.builder().id("field-1").pageId("page-1").dataField("name").build());
        when(pageFieldService.replaceFields("page-1", "user-1", List.of(request))).thenReturn(saved);

        ResponseEntity<List<PageFieldResponse>> response = pageFieldController.replaceFields("page-1", "user-1", List.of(request));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(saved);
    }
}
