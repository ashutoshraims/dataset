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

package org.opengroup.osdu.dataset.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opengroup.osdu.core.common.dms.IDmsService;
import org.opengroup.osdu.core.common.dms.constants.DatasetConstants;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.dataset.logging.AuditLogger;
import org.opengroup.osdu.dataset.model.request.GetDatasetRegistryRequest;
import org.opengroup.osdu.dataset.model.response.GetDatasetStorageInstructionsResponse;
import org.opengroup.osdu.dataset.service.DatasetDmsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
	
@RestController
@RequestMapping("/")
@Tag(name = "dataset", description = "dataset api operations")
@RequestScope
@Validated
public class DatasetDmsApi {

    @Inject
	private DpsHeaders headers;
	
	@Inject
	private DatasetDmsService datasetDmsService;
	@Inject
	private AuditLogger auditLogger;

	@Operation(summary = "${datasetDmsApi.storageInstructions.summary}", description = "${datasetDmsApi.storageInstructions.description}",
			security = {@SecurityRequirement(name = "Authorization")}, tags = { "dataset" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = { @Content(schema = @Schema(implementation = GetDatasetStorageInstructionsResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
	})
	@PostMapping("/storageInstructions")
	@PreAuthorize("@authorizationFilter.hasRole('" + DatasetConstants.DATASET_EDITOR_ROLE + "')")
	public ResponseEntity<StorageInstructionsResponse> storageInstructions(
			@Parameter(description = "subType of the kind (partition:wks:kindSubType:version)", example = "dataset--File.Generic")
			@RequestParam(value = "kindSubType") String kindSubType,
			@Parameter(description = "The Time for which Signed URL to be valid. Accepted Regex patterns are \"^[0-9]+M$\", \"^[0-9]+H$\", \"^[0-9]+D$\" denoting Integer values in Minutes, Hours, Days respectively. In absence of this parameter the URL would be valid for 1 Hour.",
					example = "5M")  @RequestParam(required = false, name = "expiryTime") String expiryTime) {
		StorageInstructionsResponse response = this.datasetDmsService.getStorageInstructions(kindSubType, expiryTime);
		this.auditLogger.readStorageInstructionsSuccess(Collections.singletonList(response.toString()));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Operation(summary = "${datasetDmsApi.retrievalInstructions.summary}", description = "${datasetDmsApi.retrievalInstructions.description}",
			security = {@SecurityRequirement(name = "Authorization")}, tags = { "dataset" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = { @Content(schema = @Schema(implementation = RetrievalInstructionsResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
	})
	@GetMapping("/retrievalInstructions")
	@PreAuthorize("@authorizationFilter.hasRole('" + DatasetConstants.DATASET_VIEWER_ROLE + "')")
	public ResponseEntity<Object> retrievalInstructions(@Parameter(description = "Dataset registry id",
			example = "opendes:dataset--File.Generic:8118591ee2")
			@RequestParam(value = "id") String datasetRegistryId, @Parameter(description = "The Time for which Signed URL to be valid. Accepted Regex patterns are \"^[0-9]+M$\", \"^[0-9]+H$\", \"^[0-9]+D$\" denoting Integer values in Minutes, Hours, Days respectively. In absence of this parameter the URL would be valid for 1 Hour.",
			example = "5M")  @RequestParam(required = false, name = "expiryTime") String expiryTime) {

		List<String> datasetRegistryIds = new ArrayList<>();
		datasetRegistryIds.add(datasetRegistryId);

		return getRetrievalInstructions(datasetRegistryIds, expiryTime);
	}

	@Operation(summary = "${datasetDmsApi.retrievalInstructionsWithPost.summary}", description = "${datasetDmsApi.retrievalInstructionsWithPost.description}",
			security = {@SecurityRequirement(name = "Authorization")}, tags = { "dataset" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = { @Content(schema = @Schema(implementation = RetrievalInstructionsResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
	})
	@PostMapping("/retrievalInstructions")
	@PreAuthorize("@authorizationFilter.hasRole('" + DatasetConstants.DATASET_VIEWER_ROLE + "')")
	public ResponseEntity<Object> retrievalInstructions(@Parameter(description = "Dataset registry ids")
			@RequestBody @Valid @NotNull GetDatasetRegistryRequest request, @Parameter(description = "The Time for which Signed URL to be valid. Accepted Regex patterns are \"^[0-9]+M$\", \"^[0-9]+H$\", \"^[0-9]+D$\" denoting Integer values in Minutes, Hours, Days respectively. In absence of this parameter the URL would be valid for 1 Hour.",
			example = "5M")  @RequestParam(required = false, name = "expiryTime") String expiryTime) {

		return getRetrievalInstructions(request.datasetRegistryIds, expiryTime);
	}

	private ResponseEntity<Object> getRetrievalInstructions(List<String> datasetRegistryIds, String expiryTime) {
		Object response = this.datasetDmsService.getRetrievalInstructions(datasetRegistryIds, expiryTime);
		this.auditLogger.readRetrievalInstructionsSuccess(Collections.singletonList(response.toString()));
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	@Operation(summary = "${datasetDmsAdminApi.revokeURL.summary}", description = "${datasetDmsAdminApi.revokeURL.description}",
			security = {@SecurityRequirement(name = "Authorization")}, tags = { "datasetDms-admin-api" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Revoked URLs successfully."),
			@ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "403", description = "User not authorized to perform the action",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class))}),
			@ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class))})
	})

	@PostMapping("/revokeURL")
	@PreAuthorize("@authorizationFilter.hasRole('" + DatasetConstants.DATASET_ADMIN_ROLE + "')")
	public ResponseEntity<Void> revokeURL(@Parameter(description = "subType of the kind (partition:wks:kindSubType:version)", example = "dataset--File.Generic")
										  @RequestParam(value = "kindSubType") String kindSubType,@RequestBody Map<String, String> revokeURLRequest) {
		datasetDmsService.revokeUrl(kindSubType, revokeURLRequest);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
