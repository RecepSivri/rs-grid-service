package com.rs.gridservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Keycloak GroupRepresentation'in disariya acilan sadelestirilmis hali.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponse {

    private String id;
    private String name;
    private String path;
    private Integer subGroupCount;
}
