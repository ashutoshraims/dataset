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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.StorageRole;
import org.opengroup.osdu.dataset.logging.AuditLogger;
import org.opengroup.osdu.dataset.model.request.CreateDatasetRegistryRequest;
import org.opengroup.osdu.dataset.model.request.GetDatasetRegistryRequest;
import org.opengroup.osdu.dataset.model.response.GetCreateUpdateDatasetRegistryResponse;
import org.opengroup.osdu.dataset.service.DatasetRegistryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/")
@Tag(name = "dataset", description = "dataset api operations")
@RequestScope
@Validated
public class DatasetRegistryApi {

	@Inject
	private DpsHeaders headers;

	@Inject
	private DatasetRegistryService dataRegistryService;

	@Inject
	private AuditLogger auditLogger;


	@Operation(summary = "${datasetRegistryApi.createOrUpdateDatasetRegistry.summary}", description = "${datasetRegistryApi.createOrUpdateDatasetRegistry.description}",
			security = {@SecurityRequirement(name = "Authorization")}, tags = { "dataset" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = { @Content(schema = @Schema(implementation = GetCreateUpdateDatasetRegistryResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
	})
	@PutMapping("/registerDataset")
	@PreAuthorize("@authorizationFilter.hasRole('" + StorageRole.CREATOR + "', '" + StorageRole.ADMIN + "')")
	public ResponseEntity<GetCreateUpdateDatasetRegistryResponse> createOrUpdateDatasetRegistry(
			@Parameter(description = "Dataset registry ids")
			@RequestBody @Valid @NotNull CreateDatasetRegistryRequest request) {

			GetCreateUpdateDatasetRegistryResponse response = this.dataRegistryService.createOrUpdateDatasetRegistry(request.datasetRegistries);
			this.auditLogger.registerDatasetSuccess(Collections.singletonList(response.toString()));
			return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@Operation(summary = "${datasetRegistryApi.getDatasetRegistry.summary}", description = "${datasetRegistryApi.getDatasetRegistry.description}",
			security = {@SecurityRequirement(name = "Authorization")}, tags = { "dataset" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = { @Content(schema = @Schema(implementation = GetCreateUpdateDatasetRegistryResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
	})
	@GetMapping("/getDatasetRegistry")	
	@PreAuthorize("@authorizationFilter.hasRole('" + StorageRole.CREATOR + "', '" + StorageRole.ADMIN + "', '" + StorageRole.VIEWER + "')")
	public ResponseEntity<GetCreateUpdateDatasetRegistryResponse> getDatasetRegistry(@Parameter(description = "Dataset registry id",
			example = "opendes:dataset--File.Generic:8118591ee2")
			@RequestParam(value = "id") String datasetRegistryId) {

			List<String> datasetRegistryIds = new ArrayList<>();
			datasetRegistryIds.add(datasetRegistryId);

			GetCreateUpdateDatasetRegistryResponse response = this.dataRegistryService.getDatasetRegistries(datasetRegistryIds);
			this.auditLogger.readDatasetRegistriesSuccess(Collections.singletonList(response.toString()));
			return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@Operation(summary = "${datasetRegistryApi.getDatasetRegistryUsingPOST.summary}", description = "${datasetRegistryApi.getDatasetRegistryUsingPOST.description}",
			security = {@SecurityRequirement(name = "Authorization")}, tags = { "dataset" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK", content = { @Content(schema = @Schema(implementation = GetCreateUpdateDatasetRegistryResponse.class)) }),
			@ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
			@ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
	})
	@PostMapping("/getDatasetRegistry")
	@PreAuthorize("@authorizationFilter.hasRole('" + StorageRole.CREATOR + "', '" + StorageRole.ADMIN + "', '" + StorageRole.VIEWER + "')")
	public ResponseEntity<GetCreateUpdateDatasetRegistryResponse> getDatasetRegistry(@Parameter(description = "Dataset registry ids")
			@RequestBody @Valid @NotNull GetDatasetRegistryRequest request) {
			GetCreateUpdateDatasetRegistryResponse response = this.dataRegistryService.getDatasetRegistries(request.datasetRegistryIds);
			this.auditLogger.readDatasetRegistriesSuccess(Collections.singletonList(response.toString()));
			return new ResponseEntity<>(response, HttpStatus.OK);
	}
}