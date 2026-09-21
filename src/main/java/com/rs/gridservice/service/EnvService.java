package com.rs.gridservice.service;

import com.rs.gridservice.dto.EnvCreateRequest;
import com.rs.gridservice.dto.EnvResponse;
import com.rs.gridservice.dto.EnvUpdateRequest;

import java.util.List;

public interface EnvService {

    EnvResponse addEnv(EnvCreateRequest request);

    EnvResponse getEnv(String envId);

    List<EnvResponse> getAllEnvs(int first, int max, String search);

    EnvResponse editEnv(String envId, EnvUpdateRequest request);

    void deleteEnv(String envId);
}
