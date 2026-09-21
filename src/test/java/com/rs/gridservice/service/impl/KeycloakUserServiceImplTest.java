package com.rs.gridservice.service.impl;

import com.rs.gridservice.config.KeycloakProperties;
import com.rs.gridservice.dto.UserCreateRequest;
import com.rs.gridservice.dto.UserResponse;
import com.rs.gridservice.dto.UserType;
import com.rs.gridservice.dto.UserUpdateRequest;
import com.rs.gridservice.exception.KeycloakOperationException;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.exception.UserAlreadyExistsException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakUserServiceImplTest {

    private static final String REALM = "user-service";

    @Mock
    private Keycloak keycloakAdminClient;
    @Mock
    private RealmResource realmResource;
    @Mock
    private UsersResource usersResource;
    @Mock
    private UserResource userResource;
    @Mock
    private RolesResource rolesResource;
    @Mock
    private RoleResource roleResource;
    @Mock
    private RoleMappingResource roleMappingResource;
    @Mock
    private RoleScopeResource roleScopeResource;

    private KeycloakUserServiceImpl service;

    @BeforeEach
    void setUp() {
        KeycloakProperties properties = new KeycloakProperties();
        properties.setRealm(REALM);
        service = new KeycloakUserServiceImpl(keycloakAdminClient, properties);

        lenient().when(keycloakAdminClient.realm(REALM)).thenReturn(realmResource);
        lenient().when(realmResource.users()).thenReturn(usersResource);
        lenient().when(usersResource.get(anyString())).thenReturn(userResource);
        lenient().when(realmResource.roles()).thenReturn(rolesResource);
        lenient().when(userResource.roles()).thenReturn(roleMappingResource);
        lenient().when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
    }

    private Response createdResponse(String userId) {
        return Response.created(URI.create("/users/" + userId)).build();
    }

    private UserCreateRequest baseCreateRequest() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("ahmet.yilmaz");
        request.setEmail("ahmet@example.com");
        request.setFirstName("Ahmet");
        request.setLastName("Yilmaz");
        request.setUserType(UserType.WEB);
        return request;
    }

    private void stubGetUserRepresentation(String userId, String username) {
        UserRepresentation representation = new UserRepresentation();
        representation.setId(userId);
        representation.setUsername(username);
        representation.setEnabled(true);
        when(userResource.toRepresentation()).thenReturn(representation);
        when(roleScopeResource.listAll()).thenReturn(List.of());
    }

    @Test
    void addUserWithNullEnabledAndNoPasswordDefaultsToEnabledTrueAndAssignsRole() {
        UserCreateRequest request = baseCreateRequest();
        request.setEnabled(null);
        request.setPassword(null);

        when(usersResource.create(any(UserRepresentation.class))).thenReturn(createdResponse("user-1"));
        RoleRepresentation role = new RoleRepresentation();
        role.setName("web");
        when(rolesResource.get("web")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(role);
        stubGetUserRepresentation("user-1", "ahmet.yilmaz");

        UserResponse response = service.addUser(request);

        assertThat(response.getId()).isEqualTo("user-1");
        verify(roleScopeResource).add(List.of(role));
    }

    @Test
    void addUserWithExplicitDisabledAndBlankPasswordSkipsCredential() {
        UserCreateRequest request = baseCreateRequest();
        request.setEnabled(Boolean.FALSE);
        request.setPassword("");
        request.setUserType(UserType.ADMIN);

        when(usersResource.create(any(UserRepresentation.class))).thenReturn(createdResponse("user-2"));
        RoleRepresentation role = new RoleRepresentation();
        role.setName("admin");
        when(rolesResource.get("admin")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(role);
        stubGetUserRepresentation("user-2", "ahmet.yilmaz");

        service.addUser(request);

        verify(usersResource).create(any(UserRepresentation.class));
    }

    @Test
    void addUserWithPasswordAndExplicitNonTemporarySetsCredential() {
        UserCreateRequest request = baseCreateRequest();
        request.setEnabled(Boolean.TRUE);
        request.setPassword("Passw0rd!");
        request.setTemporaryPassword(Boolean.FALSE);
        request.setUserType(UserType.DESKTOP);

        when(usersResource.create(any(UserRepresentation.class))).thenReturn(createdResponse("user-3"));
        RoleRepresentation role = new RoleRepresentation();
        role.setName("desktop");
        when(rolesResource.get("desktop")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(role);
        stubGetUserRepresentation("user-3", "ahmet.yilmaz");

        service.addUser(request);

        verify(usersResource).create(any(UserRepresentation.class));
    }

    @Test
    void addUserWithPasswordAndExplicitTemporaryTrueSetsTemporaryCredential() {
        UserCreateRequest request = baseCreateRequest();
        request.setPassword("Passw0rd!");
        request.setTemporaryPassword(Boolean.TRUE);

        when(usersResource.create(any(UserRepresentation.class))).thenReturn(createdResponse("user-4b"));
        RoleRepresentation role = new RoleRepresentation();
        role.setName("web");
        when(rolesResource.get("web")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(role);
        stubGetUserRepresentation("user-4b", "ahmet.yilmaz");

        service.addUser(request);

        verify(usersResource).create(any(UserRepresentation.class));
    }

    @Test
    void addUserWithPasswordAndNullTemporaryDefaultsToTemporaryTrue() {
        UserCreateRequest request = baseCreateRequest();
        request.setPassword("Passw0rd!");
        request.setTemporaryPassword(null);

        when(usersResource.create(any(UserRepresentation.class))).thenReturn(createdResponse("user-4"));
        RoleRepresentation role = new RoleRepresentation();
        role.setName("web");
        when(rolesResource.get("web")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(role);
        stubGetUserRepresentation("user-4", "ahmet.yilmaz");

        service.addUser(request);

        verify(usersResource).create(any(UserRepresentation.class));
    }

    @Test
    void addUserReturningConflictThrowsUserAlreadyExists() {
        UserCreateRequest request = baseCreateRequest();
        when(usersResource.create(any(UserRepresentation.class)))
                .thenReturn(Response.status(409).build());

        assertThatThrownBy(() -> service.addUser(request))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void addUserReturningUnexpectedStatusThrowsKeycloakOperationException() {
        UserCreateRequest request = baseCreateRequest();
        when(usersResource.create(any(UserRepresentation.class)))
                .thenReturn(Response.status(400).build());

        assertThatThrownBy(() -> service.addUser(request))
                .isInstanceOf(KeycloakOperationException.class);
    }

    @Test
    void addUserWhenKeycloakUnreachableThrowsKeycloakOperationException() {
        UserCreateRequest request = baseCreateRequest();
        when(usersResource.create(any(UserRepresentation.class)))
                .thenThrow(new ProcessingException("baglanti hatasi"));

        assertThatThrownBy(() -> service.addUser(request))
                .isInstanceOf(KeycloakOperationException.class);
    }

    @Test
    void addUserWhenUserTypeRoleMissingRollsBackCreatedUser() {
        UserCreateRequest request = baseCreateRequest();
        request.setUserType(UserType.ADMIN);

        when(usersResource.create(any(UserRepresentation.class))).thenReturn(createdResponse("user-5"));
        when(rolesResource.get("admin")).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenThrow(new NotFoundException());

        assertThatThrownBy(() -> service.addUser(request))
                .isInstanceOf(KeycloakOperationException.class)
                .hasMessageContaining("admin");

        verify(userResource).remove();
        verify(roleScopeResource, never()).add(any());
    }

    @Test
    void getUserReturnsMappedResponseWithResolvedUserType() {
        UserRepresentation representation = new UserRepresentation();
        representation.setId("user-1");
        representation.setUsername("ahmet.yilmaz");
        representation.setEmail("ahmet@example.com");
        representation.setEnabled(true);
        when(userResource.toRepresentation()).thenReturn(representation);

        RoleRepresentation webRole = new RoleRepresentation();
        webRole.setName("web");
        when(roleScopeResource.listAll()).thenReturn(List.of(webRole));

        UserResponse response = service.getUser("user-1");

        assertThat(response.getUsername()).isEqualTo("ahmet.yilmaz");
        assertThat(response.getUserType()).isEqualTo(UserType.WEB);
    }

    @Test
    void getUserWithNoMatchingRoleResolvesNullUserType() {
        UserRepresentation representation = new UserRepresentation();
        representation.setId("user-1");
        when(userResource.toRepresentation()).thenReturn(representation);

        RoleRepresentation unrelatedRole = new RoleRepresentation();
        unrelatedRole.setName("offline_access");
        when(roleScopeResource.listAll()).thenReturn(List.of(unrelatedRole));

        UserResponse response = service.getUser("user-1");

        assertThat(response.getUserType()).isNull();
    }

    @Test
    void getUserNotFoundThrowsResourceNotFoundException() {
        when(userResource.toRepresentation()).thenThrow(new NotFoundException());

        assertThatThrownBy(() -> service.getUser("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getUserWhenKeycloakUnreachableThrowsKeycloakOperationException() {
        when(userResource.toRepresentation()).thenThrow(new ProcessingException("baglanti hatasi"));

        assertThatThrownBy(() -> service.getUser("user-1"))
                .isInstanceOf(KeycloakOperationException.class);
    }

    @Test
    void getAllUsersWithoutSearchListsAllUsers() {
        UserRepresentation representation = new UserRepresentation();
        representation.setId("user-1");
        when(usersResource.list(0, 20)).thenReturn(List.of(representation));
        when(roleScopeResource.listAll()).thenReturn(List.of());

        List<UserResponse> response = service.getAllUsers(0, 20, null);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getId()).isEqualTo("user-1");
    }

    @Test
    void getAllUsersWithBlankSearchListsAllUsers() {
        when(usersResource.list(0, 20)).thenReturn(List.of());

        List<UserResponse> response = service.getAllUsers(0, 20, "   ");

        assertThat(response).isEmpty();
        verify(usersResource, never()).search(anyString(), anyInt(), anyInt());
    }

    @Test
    void getAllUsersWithSearchDelegatesToSearchApi() {
        when(usersResource.search("ahmet", 0, 20)).thenReturn(List.of());

        List<UserResponse> response = service.getAllUsers(0, 20, "ahmet");

        assertThat(response).isEmpty();
        verify(usersResource, never()).list(anyInt(), anyInt());
    }

    @Test
    void getAllUsersWhenKeycloakUnreachableThrowsKeycloakOperationException() {
        when(usersResource.list(0, 20)).thenThrow(new ProcessingException("baglanti hatasi"));

        assertThatThrownBy(() -> service.getAllUsers(0, 20, null))
                .isInstanceOf(KeycloakOperationException.class);
    }

    @Test
    void editUserWithAllFieldsSetUpdatesRepresentationAndPassword() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("new@example.com");
        request.setFirstName("Yeni");
        request.setLastName("Isim");
        request.setEnabled(Boolean.FALSE);
        request.setPassword("YeniSifre1!");
        request.setTemporaryPassword(Boolean.TRUE);

        UserRepresentation existing = new UserRepresentation();
        existing.setId("user-1");
        when(userResource.toRepresentation()).thenReturn(existing);
        when(roleScopeResource.listAll()).thenReturn(List.of());

        UserResponse response = service.editUser("user-1", request);

        assertThat(response.getId()).isEqualTo("user-1");
        verify(userResource).update(existing);
        verify(userResource).resetPassword(any());
        assertThat(existing.getEmail()).isEqualTo("new@example.com");
        assertThat(existing.getFirstName()).isEqualTo("Yeni");
        assertThat(existing.getLastName()).isEqualTo("Isim");
        assertThat(existing.isEnabled()).isFalse();
    }

    @Test
    void editUserWithPasswordAndNullTemporaryDefaultsToTemporaryTrue() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setPassword("YeniSifre1!");
        request.setTemporaryPassword(null);

        UserRepresentation existing = new UserRepresentation();
        existing.setId("user-1");
        when(userResource.toRepresentation()).thenReturn(existing);
        when(roleScopeResource.listAll()).thenReturn(List.of());

        service.editUser("user-1", request);

        verify(userResource).resetPassword(any());
    }

    @Test
    void editUserWithPasswordAndExplicitNonTemporarySetsCredential() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setPassword("YeniSifre1!");
        request.setTemporaryPassword(Boolean.FALSE);

        UserRepresentation existing = new UserRepresentation();
        existing.setId("user-1");
        when(userResource.toRepresentation()).thenReturn(existing);
        when(roleScopeResource.listAll()).thenReturn(List.of());

        service.editUser("user-1", request);

        verify(userResource).resetPassword(any());
    }

    @Test
    void editUserWithNoFieldsSetLeavesRepresentationUntouchedAndSkipsPassword() {
        UserUpdateRequest request = new UserUpdateRequest();

        UserRepresentation existing = new UserRepresentation();
        existing.setId("user-1");
        existing.setEmail("degismeyen@example.com");
        when(userResource.toRepresentation()).thenReturn(existing);
        when(roleScopeResource.listAll()).thenReturn(List.of());

        service.editUser("user-1", request);

        verify(userResource).update(existing);
        verify(userResource, never()).resetPassword(any());
        assertThat(existing.getEmail()).isEqualTo("degismeyen@example.com");
    }

    @Test
    void editUserWithBlankPasswordSkipsPasswordReset() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setPassword("");

        UserRepresentation existing = new UserRepresentation();
        existing.setId("user-1");
        when(userResource.toRepresentation()).thenReturn(existing);
        when(roleScopeResource.listAll()).thenReturn(List.of());

        service.editUser("user-1", request);

        verify(userResource, never()).resetPassword(any());
    }

    @Test
    void editUserNotFoundOnInitialLookupThrowsResourceNotFoundException() {
        when(userResource.toRepresentation()).thenThrow(new NotFoundException());

        assertThatThrownBy(() -> service.editUser("missing", new UserUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void editUserNotFoundOnUpdateThrowsResourceNotFoundException() {
        UserRepresentation existing = new UserRepresentation();
        when(userResource.toRepresentation()).thenReturn(existing);
        org.mockito.Mockito.doThrow(new NotFoundException()).when(userResource).update(existing);

        assertThatThrownBy(() -> service.editUser("user-1", new UserUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void editUserClientErrorOnUpdateThrowsKeycloakOperationException() {
        UserRepresentation existing = new UserRepresentation();
        when(userResource.toRepresentation()).thenReturn(existing);
        org.mockito.Mockito.doThrow(new jakarta.ws.rs.BadRequestException()).when(userResource).update(existing);

        assertThatThrownBy(() -> service.editUser("user-1", new UserUpdateRequest()))
                .isInstanceOf(KeycloakOperationException.class);
    }

    @Test
    void editUserProcessingExceptionOnUpdateThrowsKeycloakOperationException() {
        UserRepresentation existing = new UserRepresentation();
        when(userResource.toRepresentation()).thenReturn(existing);
        org.mockito.Mockito.doThrow(new ProcessingException("baglanti hatasi")).when(userResource).update(existing);

        assertThatThrownBy(() -> service.editUser("user-1", new UserUpdateRequest()))
                .isInstanceOf(KeycloakOperationException.class);
    }

    @Test
    void deleteUserRemovesUser() {
        service.deleteUser("user-1");

        verify(userResource).remove();
    }

    @Test
    void deleteUserNotFoundThrowsResourceNotFoundException() {
        org.mockito.Mockito.doThrow(new NotFoundException()).when(userResource).remove();

        assertThatThrownBy(() -> service.deleteUser("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteUserClientErrorThrowsKeycloakOperationException() {
        org.mockito.Mockito.doThrow(new jakarta.ws.rs.BadRequestException()).when(userResource).remove();

        assertThatThrownBy(() -> service.deleteUser("user-1"))
                .isInstanceOf(KeycloakOperationException.class);
    }

    @Test
    void deleteUserProcessingExceptionThrowsKeycloakOperationException() {
        org.mockito.Mockito.doThrow(new ProcessingException("baglanti hatasi")).when(userResource).remove();

        assertThatThrownBy(() -> service.deleteUser("user-1"))
                .isInstanceOf(KeycloakOperationException.class);
    }
}
