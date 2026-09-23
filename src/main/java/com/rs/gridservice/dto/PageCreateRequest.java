package com.rs.gridservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Yeni bir page kaydi olustururken kullanilan request body.
 */
@Getter
@Setter
public class PageCreateRequest {

    @NotBlank(message = "projectId bos olamaz")
    private String projectId;

    @NotBlank(message = "userId bos olamaz")
    private String userId;

    @NotBlank(message = "name bos olamaz")
    private String name;

    private String getApi;

    private String getAllApi;

    private String deleteApi;

    private String addApi;

    private String updateApi;

    private String batchUpdateApi;
}
