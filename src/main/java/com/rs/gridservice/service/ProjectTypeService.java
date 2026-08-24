package com.rs.gridservice.service;

import com.rs.gridservice.dto.ProjectTypeCreateRequest;
import com.rs.gridservice.dto.ProjectTypeResponse;
import com.rs.gridservice.dto.ProjectTypeUpdateRequest;

import java.util.List;

public interface ProjectTypeService {

    ProjectTypeResponse addProjectType(ProjectTypeCreateRequest request);

    ProjectTypeResponse getProjectType(String projectTypeId);

    List<ProjectTypeResponse> getAllProjectTypes(int first, int max, String search);

    ProjectTypeResponse editProjectType(String projectTypeId, ProjectTypeUpdateRequest request);

    void deleteProjectType(String projectTypeId);
}
