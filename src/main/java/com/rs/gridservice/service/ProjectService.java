package com.rs.gridservice.service;

import com.rs.gridservice.dto.ProjectCreateRequest;
import com.rs.gridservice.dto.ProjectResponse;
import com.rs.gridservice.dto.ProjectUpdateRequest;

import java.util.List;

public interface ProjectService {

    ProjectResponse addProject(ProjectCreateRequest request);

    ProjectResponse getProject(String projectId);

    /** userId'ye ait projeleri listeler. */
    List<ProjectResponse> getAllProjects(String userId, int first, int max, String search);

    /** Sadece projenin sahibi (userId) guncelleyebilir; baskasina aitse "bulunamadi" hatasi doner. */
    ProjectResponse editProject(String projectId, String userId, ProjectUpdateRequest request);

    /** Sadece projenin sahibi (userId) silebilir; baskasina aitse "bulunamadi" hatasi doner. */
    void deleteProject(String projectId, String userId);
}
