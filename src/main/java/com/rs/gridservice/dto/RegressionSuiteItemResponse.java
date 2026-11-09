package com.rs.gridservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * RegressionSuiteItemEntity'nin disariya acilan hali.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegressionSuiteItemResponse {

    private String id;
    private String projectId;
    private String pageId;
    private String endpointLabel;
    private int sortOrder;
}
