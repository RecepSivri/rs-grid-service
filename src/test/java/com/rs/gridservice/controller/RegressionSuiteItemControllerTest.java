package com.rs.gridservice.controller;

import com.rs.gridservice.dto.RegressionSuiteItemResponse;
import com.rs.gridservice.dto.RegressionSuiteItemUpsertRequest;
import com.rs.gridservice.service.RegressionSuiteItemService;
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
class RegressionSuiteItemControllerTest {

    @Mock
    private RegressionSuiteItemService regressionSuiteItemService;

    @InjectMocks
    private RegressionSuiteItemController regressionSuiteItemController;

    @Test
    void getItemsReturnsItemsFromService() {
        List<RegressionSuiteItemResponse> items = List.of(RegressionSuiteItemResponse.builder().id("item-1").projectId("project-1").build());
        when(regressionSuiteItemService.getItems("project-1", "user-1")).thenReturn(items);

        ResponseEntity<List<RegressionSuiteItemResponse>> response = regressionSuiteItemController.getItems("project-1", "user-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(items);
    }

    @Test
    void replaceItemsDelegatesToServiceAndReturnsSavedList() {
        RegressionSuiteItemUpsertRequest request = new RegressionSuiteItemUpsertRequest();
        request.setPageId("page-1");
        request.setEndpointLabel("GetAll");
        List<RegressionSuiteItemResponse> saved = List.of(RegressionSuiteItemResponse.builder().id("item-1").projectId("project-1").pageId("page-1").endpointLabel("GetAll").build());
        when(regressionSuiteItemService.replaceItems("project-1", "user-1", List.of(request))).thenReturn(saved);

        ResponseEntity<List<RegressionSuiteItemResponse>> response = regressionSuiteItemController.replaceItems("project-1", "user-1", List.of(request));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(saved);
    }
}
