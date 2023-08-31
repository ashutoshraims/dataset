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

import java.util.List;
import java.util.Map;

import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;

public interface DatasetDmsService {

    StorageInstructionsResponse getStorageInstructions(String resourceType, String expiryTime);

    default RetrievalInstructionsResponse getRetrievalInstructions(List<String> datasetRegistryIds, String expiryTime) {
        return null;
    }

    void revokeUrl(String kindSubType, Map<String, String> revokeURLRequest);
}
