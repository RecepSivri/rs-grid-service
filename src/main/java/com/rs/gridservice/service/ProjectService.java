package com.rs.gridservice.service;

import com.rs.gridservice.dto.ProjectCreateRequest;
import com.rs.gridservice.dto.ProjectResponse;
import com.rs.gridservice.dto.ProjectUpdateRequest;

import java.util.List;

public interface ProjectService {

    ProjectResponse addProject(ProjectCreateRequest request);

    ProjectResponse getProject(String projectId);

    List<ProjectResponse> getAllProjects(int first, int max, String search);

    ProjectResponse editProject(String projectId, ProjectUpdateRequest request);

    void deleteProject(String projectId);
}
