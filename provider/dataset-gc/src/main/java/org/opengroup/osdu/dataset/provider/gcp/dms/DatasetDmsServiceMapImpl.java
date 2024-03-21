/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.dataset.provider.gcp.dms;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.dataset.dms.DmsServiceProperties;
import org.opengroup.osdu.dataset.model.validation.DmsValidationDoc;
import org.opengroup.osdu.dataset.provider.gcp.config.GcpConfigProperties;
import org.opengroup.osdu.dataset.provider.gcp.model.dataset.DataSetType;
import org.opengroup.osdu.dataset.provider.gcp.model.dataset.DmsServicePropertiesEntity;
import org.opengroup.osdu.dataset.provider.gcp.model.dataset.GcpDmsServiceProperties;
import org.opengroup.osdu.dataset.provider.gcp.mappers.osm.repository.DmsServicePropertiesRepository;
import org.opengroup.osdu.dataset.provider.interfaces.IDatasetDmsServiceMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;

@Service
@RequiredArgsConstructor
public class DatasetDmsServiceMapImpl implements IDatasetDmsServiceMap {

  private final DmsServicePropertiesRepository dmsServicePropertiesRepository;
  private final GcpConfigProperties gcpConfigProperties;

  /**
   * Get DMS service properties map with `dataset type` -> `dataset provider info` content stored into the OSM storage.
   *
   * @return DMS service properties map.
   */
  @Override
  public Map<String, DmsServiceProperties> getResourceTypeToDmsServiceMap() {
    Iterable<DmsServicePropertiesEntity> dmsServiceProperties = this.dmsServicePropertiesRepository.findAll();
    String localDmsProviderBaseUrl = this.gcpConfigProperties.getDmsApiBase();
    return StreamSupport.stream(dmsServiceProperties.spliterator(), false)
        .collect(Collectors.toMap(DmsServicePropertiesEntity::getDatasetKind,
            entity -> GcpDmsServiceProperties.builder()
                .dmsServiceBaseUrl(StringUtils.isEmpty(localDmsProviderBaseUrl) ? entity.getDmsServiceBaseUrl() : localDmsProviderBaseUrl)
                .allowStorage(entity.isStorageAllowed())
                .apiKey(entity.getApiKey())
                .stagingLocationSupported(entity.isStagingLocationSupported())
                .dataSetType(mapKindToDatasetType(entity.getDatasetKind()))
                .build()));
  }

  @NotNull
  private static DataSetType mapKindToDatasetType(String datasetKind) {
    switch (datasetKind) {
      case "dataset--File.*":
        return DataSetType.FILE;
      case "dataset--FileCollection.*":
        return DataSetType.FILE_COLLECTION;
      case "dataset--ConnectedSource.*":
        return DataSetType.CONNECTED_SOURCE;
      default:
        throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
          String.format(DmsValidationDoc.KIND_SUB_TYPE_NOT_REGISTERED_ERROR, datasetKind));
    }
  }
}
