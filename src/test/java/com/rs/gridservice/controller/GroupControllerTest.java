package com.rs.gridservice.controller;

import com.rs.gridservice.dto.GroupResponse;
import com.rs.gridservice.service.GroupService;
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
class GroupControllerTest {

    @Mock
    private GroupService groupService;

    @InjectMocks
    private GroupController groupController;

    @Test
    void getAllGroupsDelegatesPagingAndSearchToService() {
        List<GroupResponse> groups = List.of(GroupResponse.builder().id("group-1").name("engineering").build());
        when(groupService.getAllGroups(0, 20, "eng")).thenReturn(groups);

        ResponseEntity<List<GroupResponse>> response = groupController.getAllGroups(0, 20, "eng");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(groups);
    }
}
