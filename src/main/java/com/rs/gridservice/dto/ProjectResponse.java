package com.rs.gridservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ProjectEntity'nin disariya acilan hali.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private String id;
    private String name;
    private String userId;
    private String technology;
    private String type;
}
