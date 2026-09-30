package com.rs.gridservice.dto;

import com.rs.gridservice.entity.DataTypeKind;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DataTypeEntity'nin disariya acilan hali.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataTypeResponse {

    private String id;
    private String name;
    private DataTypeKind kind;
}
