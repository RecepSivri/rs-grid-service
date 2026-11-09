package com.rs.gridservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRunItemResponse {

    private String pageId;
    private String pageName;
    private String endpointLabel;
    private String verdict;
    private String detail;
    private int sortOrder;
}
