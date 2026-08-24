package com.rs.gridservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ProjectTypeEntity'nin disariya acilan hali.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTypeResponse {

    private String id;
    private String name;
}
