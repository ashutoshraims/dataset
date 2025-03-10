/*
 * Copyright 2020 Google LLC
 * Copyright 2020 EPAM Systems, Inc
 * Copyright © 2021 Amazon Web Services
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

package org.opengroup.osdu.dataset.util;

public abstract class CloudStorageUtil {

  public abstract String uploadCloudFileUsingProvidedCredentials(String fileName, Object storageLocationProperties, String fileContents) throws Exception;

  public abstract String downloadCloudFileUsingDeliveryItem(Object deliveryItem) throws Exception;
}

