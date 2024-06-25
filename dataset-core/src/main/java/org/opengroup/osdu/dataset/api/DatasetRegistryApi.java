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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.core.common.model.storage.StorageRole;
import org.opengroup.osdu.dataset.model.request.CreateDatasetRegistryRequest;
import org.opengroup.osdu.dataset.model.request.GetDatasetRegistryRequest;
import org.opengroup.osdu.dataset.model.response.GetCreateUpdateDatasetRegistryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


public interface DatasetRegistryApi {
    @Operation(summary = "${datasetRegistryApi.createOrUpdateDatasetRegistry.summary}", description = "${datasetRegistryApi.createOrUpdateDatasetRegistry.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"dataset"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(schema = @Schema(implementation = GetCreateUpdateDatasetRegistryResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "404", description = "Not Found", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @PutMapping("/registerDataset")
    @PreAuthorize("@authorizationFilter.hasRole('" + StorageRole.CREATOR + "', '" + StorageRole.ADMIN + "')")
    ResponseEntity<GetCreateUpdateDatasetRegistryResponse> createOrUpdateDatasetRegistry(
            @Parameter(description = "Dataset registry ids")
            @RequestBody @Valid @NotNull CreateDatasetRegistryRequest request);

    @Operation(summary = "${datasetRegistryApi.getDatasetRegistry.summary}", description = "${datasetRegistryApi.getDatasetRegistry.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"dataset"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(schema = @Schema(implementation = GetCreateUpdateDatasetRegistryResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "404", description = "Not Found", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @GetMapping("/getDatasetRegistry")
    @PreAuthorize("@authorizationFilter.hasRole('" + StorageRole.CREATOR + "', '" + StorageRole.ADMIN + "', '" + StorageRole.VIEWER + "')")
    ResponseEntity<GetCreateUpdateDatasetRegistryResponse> getDatasetRegistry(@Parameter(description = "Dataset registry id",
            example = "opendes:dataset--File.Generic:8118591ee2")
                                                                              @RequestParam(value = "id") String datasetRegistryId);

    @Operation(summary = "${datasetRegistryApi.getDatasetRegistryUsingPOST.summary}", description = "${datasetRegistryApi.getDatasetRegistryUsingPOST.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"dataset"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(schema = @Schema(implementation = GetCreateUpdateDatasetRegistryResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "404", description = "Not Found", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @PostMapping("/getDatasetRegistry")
    @PreAuthorize("@authorizationFilter.hasRole('" + StorageRole.CREATOR + "', '" + StorageRole.ADMIN + "', '" + StorageRole.VIEWER + "')")
    ResponseEntity<GetCreateUpdateDatasetRegistryResponse> getDatasetRegistry(@Parameter(description = "Dataset registry ids")
                                                                              @RequestBody @Valid @NotNull GetDatasetRegistryRequest request);
}
