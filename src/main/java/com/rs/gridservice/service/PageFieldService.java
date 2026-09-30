package com.rs.gridservice.service;

import com.rs.gridservice.dto.PageFieldResponse;
import com.rs.gridservice.dto.PageFieldUpsertRequest;

import java.util.List;

public interface PageFieldService {

    List<PageFieldResponse> getFields(String pageId, String userId);

    List<PageFieldResponse> replaceFields(String pageId, String userId, List<PageFieldUpsertRequest> fields);
}
