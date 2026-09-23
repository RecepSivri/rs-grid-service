package com.rs.gridservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PageEntity'nin disariya acilan hali.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse {

    private String id;
    private String projectId;
    private String userId;
    private String name;
    private String getApi;
    private String getAllApi;
    private String deleteApi;
    private String addApi;
    private String updateApi;
    private String batchUpdateApi;
}
