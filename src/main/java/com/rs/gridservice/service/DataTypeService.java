package com.rs.gridservice.service;

import com.rs.gridservice.dto.DataTypeCreateRequest;
import com.rs.gridservice.dto.DataTypeResponse;
import com.rs.gridservice.dto.DataTypeUpdateRequest;

import java.util.List;

public interface DataTypeService {

    DataTypeResponse addDataType(DataTypeCreateRequest request);

    DataTypeResponse getDataType(String dataTypeId);

    List<DataTypeResponse> getAllDataTypes(int first, int max, String search);

    DataTypeResponse editDataType(String dataTypeId, DataTypeUpdateRequest request);

    void deleteDataType(String dataTypeId);
}
