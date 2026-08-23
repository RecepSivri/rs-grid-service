package com.rs.gridservice.service.impl;

import com.rs.gridservice.config.KeycloakProperties;
import com.rs.gridservice.dto.UserCreateRequest;
import com.rs.gridservice.dto.UserResponse;
import com.rs.gridservice.dto.UserType;
import com.rs.gridservice.dto.UserUpdateRequest;
import com.rs.gridservice.exception.KeycloakOperationException;
import com.rs.gridservice.exception.ResourceNotFoundException;
import com.rs.gridservice.exception.UserAlreadyExistsException;
import com.rs.gridservice.service.UserService;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Keycloak Admin REST API'sini (keycloak-admin-client uzerinden) wrap eden
 * kullanici yonetim servisi implementasyonu.
 *
 * Butun operasyonlar, application.yml -> rs-grid.keycloak.realm altinda
 * tanimli realm uzerinde calisir.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakUserServiceImpl implements UserService {

    private final Keycloak keycloakAdminClient;
    private final KeycloakProperties keycloakProperties;

    private UsersResource usersResource() {
        return keycloakAdminClient.realm(keycloakProperties.getRealm()).users();
    }

    @Override
    public UserResponse addUser(UserCreateRequest request) {
        UserRepresentation representation = new UserRepresentation();
        representation.setUsername(request.getUsername());
        representation.setEmail(request.getEmail());
        representation.setFirstName(request.getFirstName());
        representation.setLastName(request.getLastName());
        representation.setEnabled(Boolean.TRUE.equals(request.getEnabled()) || request.getEnabled() == null);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.getPassword());
            credential.setTemporary(request.getTemporaryPassword() == null || request.getTemporaryPassword());
            representation.setCredentials(Collections.singletonList(credential));
        }

        try (Response response = usersResource().create(representation)) {
            if (response.getStatus() == 201) {
                String userId = CreatedResponseUtil.getCreatedId(response);
                assignUserTypeRole(userId, request.getUserType());
                log.info("Keycloak kullanicisi olusturuldu: username={}, id={}, userType={}",
                        request.getUsername(), userId, request.getUserType());
                return getUser(userId);
            }
            if (response.getStatus() == 409) {
                throw new UserAlreadyExistsException(
                        "'" + request.getUsername() + "' kullanici adi (veya email) zaten kullaniliyor");
            }
            throw new KeycloakOperationException(
                    "Kullanici olusturulamadi, Keycloak status=" + response.getStatus(), null);
        } catch (ProcessingException e) {
            throw new KeycloakOperationException("Keycloak'a baglanilamadi", e);
        }
    }

    @Override
    public UserResponse getUser(String userId) {
        try {
            UserRepresentation representation = usersResource().get(userId).toRepresentation();
            return toResponse(representation);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("Kullanici bulunamadi: id=" + userId);
        } catch (ProcessingException e) {
            throw new KeycloakOperationException("Keycloak'a baglanilamadi", e);
        }
    }

    @Override
    public List<UserResponse> getAllUsers(int first, int max, String search) {
        try {
            List<UserRepresentation> representations = (search == null || search.isBlank())
                    ? usersResource().list(first, max)
                    : usersResource().search(search, first, max);

            return representations.stream()
                    .map(this::toResponse)
                    .toList();
        } catch (ProcessingException e) {
            throw new KeycloakOperationException("Keycloak'a baglanilamadi", e);
        }
    }

    @Override
    public UserResponse editUser(String userId, UserUpdateRequest request) {
        UserResource userResource = usersResource().get(userId);
        UserRepresentation representation;
        try {
            representation = userResource.toRepresentation();
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("Kullanici bulunamadi: id=" + userId);
        }

        if (request.getEmail() != null) {
            representation.setEmail(request.getEmail());
        }
        if (request.getFirstName() != null) {
            representation.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            representation.setLastName(request.getLastName());
        }
        if (request.getEnabled() != null) {
            representation.setEnabled(request.getEnabled());
        }

        try {
            userResource.update(representation);

            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(request.getPassword());
                credential.setTemporary(request.getTemporaryPassword() == null || request.getTemporaryPassword());
                userResource.resetPassword(credential);
            }

            log.info("Keycloak kullanicisi guncellendi: id={}", userId);
            return getUser(userId);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("Kullanici bulunamadi: id=" + userId);
        } catch (ClientErrorException e) {
            throw new KeycloakOperationException(
                    "Kullanici guncellenemedi, Keycloak status=" + e.getResponse().getStatus(), e);
        } catch (ProcessingException e) {
            throw new KeycloakOperationException("Keycloak'a baglanilamadi", e);
        }
    }

    @Override
    public void deleteUser(String userId) {
        try {
            usersResource().get(userId).remove();
            log.info("Keycloak kullanicisi silindi: id={}", userId);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("Kullanici bulunamadi: id=" + userId);
        } catch (ClientErrorException e) {
            throw new KeycloakOperationException(
                    "Kullanici silinemedi, Keycloak status=" + e.getResponse().getStatus(), e);
        } catch (ProcessingException e) {
            throw new KeycloakOperationException("Keycloak'a baglanilamadi", e);
        }
    }

    private UserResponse toResponse(UserRepresentation representation) {
        return UserResponse.builder()
                .id(representation.getId())
                .username(representation.getUsername())
                .email(representation.getEmail())
                .firstName(representation.getFirstName())
                .lastName(representation.getLastName())
                .enabled(representation.isEnabled())
                .emailVerified(representation.isEmailVerified())
                .createdTimestamp(representation.getCreatedTimestamp())
                .userType(resolveUserType(representation.getId()))
                .build();
    }

    /** Kullaniciya admin/web/desktop realm rolunden hangisi atanmissa onu dondurur, hicbiri yoksa null. */
    private UserType resolveUserType(String userId) {
        List<RoleRepresentation> roles = usersResource().get(userId).roles().realmLevel().listAll();
        for (RoleRepresentation role : roles) {
            for (UserType type : UserType.values()) {
                if (type.roleName().equals(role.getName())) {
                    return type;
                }
            }
        }
        return null;
    }

    private void assignUserTypeRole(String userId, UserType userType) {
        RoleRepresentation role;
        try {
            role = keycloakAdminClient.realm(keycloakProperties.getRealm())
                    .roles().get(userType.roleName()).toRepresentation();
        } catch (NotFoundException e) {
            usersResource().get(userId).remove();
            throw new KeycloakOperationException(
                    "'" + userType.roleName() + "' realm rolu Keycloak'ta bulunamadi. "
                            + "Once Keycloak'ta bu rolu olusturmaniz gerekiyor (Realm roles -> Create role). "
                            + "Kullanici olusturulmadi.", e);
        }
        usersResource().get(userId).roles().realmLevel().add(Collections.singletonList(role));
    }
}
