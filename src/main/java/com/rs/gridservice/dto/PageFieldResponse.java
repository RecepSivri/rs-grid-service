package com.rs.gridservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PageFieldEntity'nin disariya acilan hali.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageFieldResponse {

    private String id;
    private String pageId;
    private String dataField;
    private String caption;
    private String inputType;
    private boolean visible;
    private boolean readonly;
    private String dataTypeId;
    private String lookupTargetPageId;
    private String lookupValueField;
    private String lookupTemplate;
    private int sortOrder;
    private boolean required;
}
