package com.rs.gridservice.service;

import com.rs.gridservice.dto.GroupResponse;

import java.util.List;

/**
 * Keycloak Admin API'sini wrap eden grup listeleme servisi.
 */
public interface GroupService {

    List<GroupResponse> getAllGroups(int first, int max, String search);
}
