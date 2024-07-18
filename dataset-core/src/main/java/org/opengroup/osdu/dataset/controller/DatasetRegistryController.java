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

package org.opengroup.osdu.dataset.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.dataset.api.DatasetRegistryApi;
import org.opengroup.osdu.dataset.logging.AuditLogger;
import org.opengroup.osdu.dataset.model.request.CreateDatasetRegistryRequest;
import org.opengroup.osdu.dataset.model.request.GetDatasetRegistryRequest;
import org.opengroup.osdu.dataset.model.response.GetCreateUpdateDatasetRegistryResponse;
import org.opengroup.osdu.dataset.service.DatasetRegistryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/")
@Tag(name = "dataset", description = "dataset api operations")
@RequestScope
@Validated
public class DatasetRegistryController implements DatasetRegistryApi {

    @Inject
    private DpsHeaders headers;

    @Inject
    private DatasetRegistryService dataRegistryService;

    @Inject
    private AuditLogger auditLogger;


    @Override
    public ResponseEntity<GetCreateUpdateDatasetRegistryResponse> createOrUpdateDatasetRegistry(CreateDatasetRegistryRequest request) {
        GetCreateUpdateDatasetRegistryResponse response = this.dataRegistryService.createOrUpdateDatasetRegistry(request.datasetRegistries);
        this.auditLogger.registerDatasetSuccess(Collections.singletonList(response.toString()));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<GetCreateUpdateDatasetRegistryResponse> getDatasetRegistry(String datasetRegistryId) {
        List<String> datasetRegistryIds = new ArrayList<>();
        datasetRegistryIds.add(datasetRegistryId);
        GetCreateUpdateDatasetRegistryResponse response = this.dataRegistryService.getDatasetRegistries(datasetRegistryIds);
        this.auditLogger.readDatasetRegistriesSuccess(Collections.singletonList(response.toString()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<GetCreateUpdateDatasetRegistryResponse> getDatasetRegistry(GetDatasetRegistryRequest request) {
        GetCreateUpdateDatasetRegistryResponse response = this.dataRegistryService.getDatasetRegistries(request.datasetRegistryIds);
        this.auditLogger.readDatasetRegistriesSuccess(Collections.singletonList(response.toString()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteMetadataById(String id) {
        dataRegistryService.deleteMetadataRecord(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}