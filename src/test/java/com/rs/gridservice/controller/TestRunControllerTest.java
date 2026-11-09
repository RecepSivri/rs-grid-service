package com.rs.gridservice.controller;

import com.rs.gridservice.dto.TestRunItemUpsertRequest;
import com.rs.gridservice.dto.TestRunResponse;
import com.rs.gridservice.dto.TestRunUpsertRequest;
import com.rs.gridservice.service.TestRunService;
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
class TestRunControllerTest {

    @Mock
    private TestRunService testRunService;

    @InjectMocks
    private TestRunController testRunController;

    @Test
    void getRunsForUserReturnsRunsFromService() {
        List<TestRunResponse> runs = List.of(TestRunResponse.builder().id("run-1").projectId("project-1").mode("smoke").build());
        when(testRunService.getRunsForUser("user-1")).thenReturn(runs);

        ResponseEntity<List<TestRunResponse>> response = testRunController.getRunsForUser("user-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(runs);
    }

    @Test
    void replaceRunDelegatesModeProjectIdUserIdAndBodyToService() {
        TestRunItemUpsertRequest item = new TestRunItemUpsertRequest();
        item.setPageId("page-1");
        item.setPageName("Patients");
        item.setEndpointLabel("GetAll");
        item.setVerdict("pass");
        TestRunUpsertRequest request = new TestRunUpsertRequest();
        request.setEnvUrl("http://x");
        request.setItems(List.of(item));
        TestRunResponse saved = TestRunResponse.builder().id("run-1").projectId("project-1").mode("regression").build();
        when(testRunService.replaceRun("project-1", "regression", "user-1", request)).thenReturn(saved);

        ResponseEntity<TestRunResponse> response = testRunController.replaceRun("regression", "project-1", "user-1", request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(saved);
    }
}
