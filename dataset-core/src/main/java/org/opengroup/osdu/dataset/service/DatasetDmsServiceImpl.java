// Copyright © 2021 Amazon Web Services
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.dataset.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.dataset.dms.DmsException;
import org.opengroup.osdu.dataset.dms.DmsServiceProperties;
import org.opengroup.osdu.dataset.dms.IDmsFactory;
import org.opengroup.osdu.dataset.dms.IDmsProvider;
import org.opengroup.osdu.dataset.model.request.GetDatasetRegistryRequest;
import org.opengroup.osdu.dataset.model.validation.DmsValidationDoc;
import org.opengroup.osdu.dataset.provider.interfaces.IDatasetDmsServiceMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static org.opengroup.osdu.dataset.util.ExceptionUtils.handleDmsException;

@Service
@RequiredArgsConstructor
public class DatasetDmsServiceImpl implements DatasetDmsService {

    @Inject
    private DpsHeaders headers;

    @Inject
    private IDmsFactory dmsFactory;

    @Inject
    private IDatasetDmsServiceMap dmsServiceMap;

    private final HttpResponseBodyMapper bodyMapper;

    @Override
    public StorageInstructionsResponse getStorageInstructions(String kindSubType, String expiryTime) {

        Map<String, DmsServiceProperties> kindSubTypeToDmsServiceMap = dmsServiceMap.getResourceTypeToDmsServiceMap();

        DmsServiceProperties dmsServiceProperties = null;
        
        String kindSubTypeCatchAll = getKindSubTypeCatchAll(kindSubType);
        String dmsMapId = null;

        if (kindSubTypeToDmsServiceMap.containsKey(kindSubType)) {
            dmsMapId = kindSubType;
        }
        else if (kindSubTypeToDmsServiceMap.containsKey(kindSubTypeCatchAll)) {
            dmsMapId = kindSubTypeCatchAll;
        }
        
        dmsServiceProperties = kindSubTypeToDmsServiceMap.get(dmsMapId);

        if (dmsServiceProperties == null) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    String.format(DmsValidationDoc.RESOURCE_TYPE_NOT_REGISTERED_ERROR, kindSubType));
        }

        if (!dmsServiceProperties.isAllowStorage()) {
            HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
            throw new AppException(status.value(), "DMS - Storage Not Supported",
                    String.format(DmsValidationDoc.DMS_STORAGE_NOT_SUPPORTED_ERROR, kindSubType));
        }

        StorageInstructionsResponse response = null;

        try {

            IDmsProvider dmsProvider = dmsFactory.create(headers, dmsServiceProperties);
            response = dmsProvider.getStorageInstructions(expiryTime);

        } catch (DmsException e) {
            handleDmsException(e);
        }

        return response;
    }

    @Override
    public RetrievalInstructionsResponse getRetrievalInstructions(List<String> datasetRegistryIds, String expiryTime) {
        Map<String, DmsServiceProperties> kindSubTypeToDmsServiceMap = dmsServiceMap.getResourceTypeToDmsServiceMap();
        HashMap<String, GetDatasetRegistryRequest> datasetRegistryRequestMap =
                segregateDatasetIdsToDms(datasetRegistryIds, kindSubTypeToDmsServiceMap);

        RetrievalInstructionsResponse response = new RetrievalInstructionsResponse();

        for (Map.Entry<String,GetDatasetRegistryRequest> datasetRegistryRequestEntry : datasetRegistryRequestMap.entrySet()) {
            try {
                IDmsProvider dmsProvider = dmsFactory.create(headers, kindSubTypeToDmsServiceMap.get(datasetRegistryRequestEntry.getKey()));
                RetrievalInstructionsResponse entryResponse = dmsProvider.getRetrievalInstructions(datasetRegistryRequestEntry.getValue(), expiryTime);
                response.getDatasets().addAll(entryResponse.getDatasets());
            }
            catch(DmsException e) {
                handleDmsException(e);
            }
        }
        return response;
    }

    @Override
    public void revokeUrl(String kindSubType, Map<String, String> revokeURLRequest) {
        Map<String, DmsServiceProperties> kindSubTypeToDmsServiceMap = dmsServiceMap.getResourceTypeToDmsServiceMap();

        DmsServiceProperties dmsServiceProperties = null;

        String kindSubTypeCatchAll = getKindSubTypeCatchAll(kindSubType);
        String dmsMapId = null;

        if (kindSubTypeToDmsServiceMap.containsKey(kindSubType)) {
            dmsMapId = kindSubType;
        }
        else if (kindSubTypeToDmsServiceMap.containsKey(kindSubTypeCatchAll)) {
            dmsMapId = kindSubTypeCatchAll;
        }

        dmsServiceProperties = kindSubTypeToDmsServiceMap.get(dmsMapId);

        if (dmsServiceProperties == null) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    String.format(DmsValidationDoc.RESOURCE_TYPE_NOT_REGISTERED_ERROR, kindSubType));
        }
        try {

            IDmsProvider dmsProvider = dmsFactory.create(headers, dmsServiceProperties);
            dmsProvider.revokeUrl(revokeURLRequest);

        } catch (DmsException e) {
            handleDmsException(e);
        }

    }


    private HashMap<String, GetDatasetRegistryRequest> segregateDatasetIdsToDms(List<String> datasetRegistryIds, Map<String, DmsServiceProperties> kindSubTypeToDmsServiceMap) {
        HashMap<String, GetDatasetRegistryRequest> datasetRegistryRequestMap = new HashMap<>();

        for (String datasetRegistryId : datasetRegistryIds) {

            if (!Record.isRecordIdValidFormatAndTenant(datasetRegistryId, headers.getPartitionId())) {
                throw new AppException(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), String.format("Dataset Registry: '%s' is an Invalid ID", datasetRegistryId), datasetRegistryId);
            }

            String kindSubType = getKindSubTypeFromID(datasetRegistryId);
            String kindSubTypeCatchAll = getKindSubTypeCatchAll(kindSubType);
            String dmsMapId = null;

            if (kindSubTypeToDmsServiceMap.containsKey(kindSubType)) {
                dmsMapId = kindSubType;
            }
            else if (kindSubTypeToDmsServiceMap.containsKey(kindSubTypeCatchAll)) {
                dmsMapId = kindSubTypeCatchAll;
            }
            else {
                throw new AppException(HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                String.format(DmsValidationDoc.KIND_SUB_TYPE_NOT_REGISTERED_ERROR, kindSubType));
            }

            if (!datasetRegistryRequestMap.containsKey(dmsMapId)) {
                GetDatasetRegistryRequest request = new GetDatasetRegistryRequest();
                request.datasetRegistryIds = new ArrayList<String>();
                request.datasetRegistryIds.add(datasetRegistryId);
                datasetRegistryRequestMap.put(dmsMapId, request);
            }
            else {
                GetDatasetRegistryRequest request = datasetRegistryRequestMap.get(dmsMapId);
                request.datasetRegistryIds.add(datasetRegistryId);
            }
        }
        return datasetRegistryRequestMap;
    }

    private String getKindSubTypeFromID(String id) {
        String[] idSplitByColon = id.split(":");

        String kindSubType = idSplitByColon[1]; //grab GroupType/IndividualType

        return kindSubType;
    }

    private String getKindSubTypeCatchAll(String kindSubType) {
        String[] splitByPeriod = kindSubType.split("\\.");

        String kindSubTypeCatchAll = splitByPeriod[0] + ".*";

        return kindSubTypeCatchAll;
    }
}
