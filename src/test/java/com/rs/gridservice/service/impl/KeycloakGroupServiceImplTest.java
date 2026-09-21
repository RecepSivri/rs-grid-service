package com.rs.gridservice.service.impl;

import com.rs.gridservice.config.KeycloakProperties;
import com.rs.gridservice.dto.GroupResponse;
import com.rs.gridservice.exception.KeycloakOperationException;
import jakarta.ws.rs.ProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakGroupServiceImplTest {

    private static final String REALM = "user-service";

    @Mock
    private Keycloak keycloakAdminClient;
    @Mock
    private RealmResource realmResource;
    @Mock
    private GroupsResource groupsResource;

    private KeycloakGroupServiceImpl service;

    @BeforeEach
    void setUp() {
        KeycloakProperties properties = new KeycloakProperties();
        properties.setRealm(REALM);
        service = new KeycloakGroupServiceImpl(keycloakAdminClient, properties);

        lenient().when(keycloakAdminClient.realm(REALM)).thenReturn(realmResource);
        lenient().when(realmResource.groups()).thenReturn(groupsResource);
    }

    @Test
    void getAllGroupsWithNullSearchListsWithoutFilter() {
        when(groupsResource.groups(0, 20)).thenReturn(List.of());

        List<GroupResponse> response = service.getAllGroups(0, 20, null);

        assertThat(response).isEmpty();
        verify(groupsResource, never()).groups(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void getAllGroupsWithBlankSearchListsWithoutFilter() {
        when(groupsResource.groups(0, 20)).thenReturn(List.of());

        List<GroupResponse> response = service.getAllGroups(0, 20, "   ");

        assertThat(response).isEmpty();
    }

    @Test
    void getAllGroupsWithSearchDelegatesToFilteredApi() {
        when(groupsResource.groups("eng", 0, 20)).thenReturn(List.of());

        service.getAllGroups(0, 20, "eng");

        verify(groupsResource).groups("eng", 0, 20);
        verify(groupsResource, never()).groups(0, 20);
    }

    @Test
    void toResponseUsesSubGroupCountWhenPresent() {
        GroupRepresentation representation = new GroupRepresentation();
        representation.setId("group-1");
        representation.setName("engineering");
        representation.setPath("/engineering");
        representation.setSubGroupCount(3L);
        when(groupsResource.groups(0, 20)).thenReturn(List.of(representation));

        List<GroupResponse> response = service.getAllGroups(0, 20, null);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getSubGroupCount()).isEqualTo(3);
    }

    @Test
    void toResponseFallsBackToSubGroupsSizeWhenCountMissing() {
        GroupRepresentation subGroup = new GroupRepresentation();
        GroupRepresentation representation = new GroupRepresentation();
        representation.setId("group-1");
        representation.setSubGroupCount(null);
        representation.setSubGroups(List.of(subGroup, subGroup));
        when(groupsResource.groups(0, 20)).thenReturn(List.of(representation));

        List<GroupResponse> response = service.getAllGroups(0, 20, null);

        assertThat(response.get(0).getSubGroupCount()).isEqualTo(2);
    }

    @Test
    void toResponseDefaultsToZeroWhenCountMissingAndNoSubGroups() {
        GroupRepresentation representation = new GroupRepresentation();
        representation.setId("group-1");
        representation.setSubGroupCount(null);

        when(groupsResource.groups(0, 20)).thenReturn(List.of(representation));

        List<GroupResponse> response = service.getAllGroups(0, 20, null);

        assertThat(response.get(0).getSubGroupCount()).isEqualTo(0);
    }

    @Test
    void getAllGroupsWhenKeycloakUnreachableThrowsKeycloakOperationException() {
        when(groupsResource.groups(0, 20)).thenThrow(new ProcessingException("baglanti hatasi"));

        assertThatThrownBy(() -> service.getAllGroups(0, 20, null))
                .isInstanceOf(KeycloakOperationException.class);
    }
}
