package com.rs.gridservice.service;

import com.rs.gridservice.dto.TechnologyCreateRequest;
import com.rs.gridservice.dto.TechnologyResponse;
import com.rs.gridservice.dto.TechnologyUpdateRequest;

import java.util.List;

public interface TechnologyService {

    TechnologyResponse addTechnology(TechnologyCreateRequest request);

    TechnologyResponse getTechnology(String technologyId);

    List<TechnologyResponse> getAllTechnologies(int first, int max, String search);

    TechnologyResponse editTechnology(String technologyId, TechnologyUpdateRequest request);

    void deleteTechnology(String technologyId);
}
