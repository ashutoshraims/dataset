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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.opengroup.osdu.core.common.dms.constants.DatasetConstants;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.AppError;
import org.opengroup.osdu.dataset.controller.DatasetDmsController;
import org.opengroup.osdu.dataset.model.request.GetDatasetRegistryRequest;
import org.opengroup.osdu.dataset.model.response.GetDatasetStorageInstructionsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@RequestMapping("/")
@Tag(name = "dataset", description = "dataset api operations")
@Validated
public interface DatasetDmsApi {
    @Operation(summary = "${datasetDmsApi.storageInstructions.summary}", description = "${datasetDmsApi.storageInstructions.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"dataset"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(schema = @Schema(implementation = GetDatasetStorageInstructionsResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "404", description = "Not Found", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @PostMapping("/storageInstructions")
    @PreAuthorize("@authorizationFilter.hasRole('" + DatasetConstants.DATASET_EDITOR_ROLE + "', '" + DatasetConstants.DATASET_ADMIN_ROLE + "')")
    ResponseEntity<StorageInstructionsResponse> storageInstructions(
            @Parameter(description = "subType of the kind (partition:wks:kindSubType:version)", example = "dataset--File.Generic")
            @RequestParam(value = "kindSubType") String kindSubType,
            @Parameter(description = "The Time for which Signed URL to be valid. Accepted Regex patterns are \"^[0-9]+M$\", \"^[0-9]+H$\", \"^[0-9]+D$\" denoting Integer values in Minutes, Hours, Days respectively. In absence of this parameter the URL would be valid for 1 Hour.",
                    example = "5M") @Valid @Pattern(regexp = DatasetDmsController.EXPIRY_TIME_PATTERN) @RequestParam(required = false, name = "expiryTime") String expiryTime);

    @Operation(summary = "${datasetDmsApi.retrievalInstructions.summary}", description = "${datasetDmsApi.retrievalInstructions.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"dataset"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(schema = @Schema(implementation = RetrievalInstructionsResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "404", description = "Not Found", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @GetMapping("/retrievalInstructions")
    @PreAuthorize("@authorizationFilter.hasRole('" + DatasetConstants.DATASET_VIEWER_ROLE + "', '" + DatasetConstants.DATASET_EDITOR_ROLE + "', '" + DatasetConstants.DATASET_ADMIN_ROLE + "')")
    ResponseEntity<Object> retrievalInstructions(@Parameter(description = "Dataset registry id",
            example = "opendes:dataset--File.Generic:8118591ee2")
                                                 @RequestParam(value = "id") String datasetRegistryId, @Parameter(description = "The Time for which Signed URL to be valid. Accepted Regex patterns are \"^[0-9]+M$\", \"^[0-9]+H$\", \"^[0-9]+D$\" denoting Integer values in Minutes, Hours, Days respectively. In absence of this parameter the URL would be valid for 1 Hour.",
            example = "5M") @Valid @Pattern(regexp = DatasetDmsController.EXPIRY_TIME_PATTERN) @RequestParam(required = false, name = "expiryTime") String expiryTime);

    @Operation(summary = "${datasetDmsApi.retrievalInstructionsWithPost.summary}", description = "${datasetDmsApi.retrievalInstructionsWithPost.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"dataset"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {@Content(schema = @Schema(implementation = RetrievalInstructionsResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "404", description = "Not Found", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @PostMapping("/retrievalInstructions")
    @PreAuthorize("@authorizationFilter.hasRole('" + DatasetConstants.DATASET_VIEWER_ROLE + "', '" + DatasetConstants.DATASET_EDITOR_ROLE + "', '" + DatasetConstants.DATASET_ADMIN_ROLE + "')")
    ResponseEntity<Object> retrievalInstructions(@Parameter(description = "Dataset registry ids")
                                                 @RequestBody @Valid @NotNull GetDatasetRegistryRequest request, @Parameter(description = "The Time for which Signed URL to be valid. Accepted Regex patterns are \"^[0-9]+M$\", \"^[0-9]+H$\", \"^[0-9]+D$\" denoting Integer values in Minutes, Hours, Days respectively. In absence of this parameter the URL would be valid for 1 Hour.",
            example = "5M") @Valid @Pattern(regexp = DatasetDmsController.EXPIRY_TIME_PATTERN) @RequestParam(required = false, name = "expiryTime") String expiryTime);

    @Operation(summary = "${datasetDmsApi.revokeURL.summary}", description = "${datasetDmsApi.revokeURL.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = {"dataset"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Revoked URLs successfully."),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway", content = {@Content(schema = @Schema(implementation = AppError.class))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable", content = {@Content(schema = @Schema(implementation = AppError.class))})
    })
    @PostMapping("/revokeURL")
    @PreAuthorize("@authorizationFilter.hasRole('" + DatasetConstants.DATASET_ADMIN_ROLE + "')")
    ResponseEntity<Void> revokeURL(@Parameter(description = "subType of the kind (partition:wks:kindSubType:version)", example = "dataset--File.Generic")
                                   @RequestParam(value = "kindSubType") String kindSubType, @RequestBody Map<String, String> revokeURLRequest);
}
