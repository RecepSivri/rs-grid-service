package com.rs.gridservice.service.impl;

import com.rs.gridservice.config.KeycloakProperties;
import com.rs.gridservice.dto.GroupResponse;
import com.rs.gridservice.exception.KeycloakOperationException;
import com.rs.gridservice.service.GroupService;
import jakarta.ws.rs.ProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Keycloak Admin REST API'sini (keycloak-admin-client uzerinden) wrap eden
 * grup listeleme servisi implementasyonu.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakGroupServiceImpl implements GroupService {

    private final Keycloak keycloakAdminClient;
    private final KeycloakProperties keycloakProperties;

    @Override
    public List<GroupResponse> getAllGroups(int first, int max, String search) {
        try {
            List<GroupRepresentation> groups = (search == null || search.isBlank())
                    ? keycloakAdminClient.realm(keycloakProperties.getRealm()).groups().groups(first, max)
                    : keycloakAdminClient.realm(keycloakProperties.getRealm()).groups().groups(search, first, max);

            return groups.stream().map(this::toResponse).toList();
        } catch (ProcessingException e) {
            throw new KeycloakOperationException("Keycloak'a baglanilamadi", e);
        }
    }

    private GroupResponse toResponse(GroupRepresentation representation) {
        Integer subGroupCount = representation.getSubGroupCount() != null
                ? representation.getSubGroupCount().intValue()
                : (representation.getSubGroups() != null ? representation.getSubGroups().size() : 0);

        return GroupResponse.builder()
                .id(representation.getId())
                .name(representation.getName())
                .path(representation.getPath())
                .subGroupCount(subGroupCount)
                .build();
    }
}
