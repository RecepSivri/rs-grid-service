package com.rs.gridservice.service;

import com.rs.gridservice.dto.EnvCreateRequest;
import com.rs.gridservice.dto.EnvResponse;
import com.rs.gridservice.dto.EnvUpdateRequest;

import java.util.List;

public interface EnvService {

    EnvResponse addEnv(EnvCreateRequest request);

    EnvResponse getEnv(String envId);

    /** userId'ye ait env kayitlarini listeler. */
    List<EnvResponse> getAllEnvs(String userId, int first, int max, String search);

    /** Sadece kaydin sahibi (userId) guncelleyebilir; baskasina aitse "bulunamadi" hatasi doner. */
    EnvResponse editEnv(String envId, String userId, EnvUpdateRequest request);

    /** Sadece kaydin sahibi (userId) silebilir; baskasina aitse "bulunamadi" hatasi doner. */
    void deleteEnv(String envId, String userId);
}
