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

import jakarta.inject.Inject;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.dataset.api.DatasetDmsApi;
import org.opengroup.osdu.dataset.logging.AuditLogger;
import org.opengroup.osdu.dataset.model.request.GetDatasetRegistryRequest;
import org.opengroup.osdu.dataset.service.DatasetDmsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@RestController
@RequestScope
public class DatasetDmsController implements DatasetDmsApi {

	public static final String EXPIRY_TIME_PATTERN = "\\d+([mhd]|[MHD])$";
	@Inject
	private DpsHeaders headers;
	@Inject
	private DatasetDmsService datasetDmsService;
	@Inject
	private AuditLogger auditLogger;

	@Override
	public ResponseEntity<StorageInstructionsResponse> storageInstructions(String kindSubType, String expiryTime) {
		StorageInstructionsResponse response = this.datasetDmsService.getStorageInstructions(kindSubType, expiryTime);
		this.auditLogger.readStorageInstructionsSuccess(Collections.singletonList(response.toString()));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> retrievalInstructions(String datasetRegistryId, String expiryTime) {
		List<String> datasetRegistryIds = new ArrayList<>();
		datasetRegistryIds.add(datasetRegistryId);
		return getRetrievalInstructions(datasetRegistryIds, expiryTime);
	}

	@Override
	public ResponseEntity<Object> retrievalInstructions(GetDatasetRegistryRequest request, String expiryTime) {
		return getRetrievalInstructions(request.datasetRegistryIds, expiryTime);
	}

	@Override
	public ResponseEntity<Void> revokeURL(String kindSubType, Map<String, String> revokeURLRequest) {
		datasetDmsService.revokeUrl(kindSubType, revokeURLRequest);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	private ResponseEntity<Object> getRetrievalInstructions(List<String> datasetRegistryIds, String expiryTime) {
		Object response = this.datasetDmsService.getRetrievalInstructions(datasetRegistryIds, expiryTime);
		this.auditLogger.readRetrievalInstructionsSuccess(Collections.singletonList(response.toString()));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
