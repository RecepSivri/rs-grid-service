package com.rs.gridservice.service;

import com.rs.gridservice.dto.RegressionSuiteItemResponse;
import com.rs.gridservice.dto.RegressionSuiteItemUpsertRequest;

import java.util.List;

public interface RegressionSuiteItemService {

    List<RegressionSuiteItemResponse> getItems(String projectId, String userId);

    List<RegressionSuiteItemResponse> replaceItems(String projectId, String userId, List<RegressionSuiteItemUpsertRequest> items);
}
