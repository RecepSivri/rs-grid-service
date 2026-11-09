package com.rs.gridservice.service;

import com.rs.gridservice.dto.ProjectAuthResponse;
import com.rs.gridservice.dto.ProjectAuthUpsertRequest;

public interface ProjectAuthService {

    ProjectAuthResponse getAuth(String projectId, String userId);

    ProjectAuthResponse saveAuth(String projectId, String userId, ProjectAuthUpsertRequest request);

    void clearAuth(String projectId, String userId);
}
