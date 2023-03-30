/*
 * Copyright 2022  Microsoft Corporation
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

package org.opengroup.osdu.dataset.provider.azure.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opengroup.osdu.core.common.http.IHttpClient;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.dataset.dms.DmsService;
import org.opengroup.osdu.dataset.dms.DmsServiceProperties;

public class AzureDmsService extends DmsService {

  private static final String RETRIEVAL_INSTRUCTIONS_PATH = "/retrievalInstructions";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final DmsServiceProperties dmsServiceProperties;
  private final IHttpClient httpClient;
  private final DpsHeaders headers;

  public AzureDmsService(DmsServiceProperties dmsServiceProperties, IHttpClient httpClient,
                         DpsHeaders headers) {
    super(dmsServiceProperties, httpClient, headers);
    this.dmsServiceProperties = dmsServiceProperties;
    this.httpClient = httpClient;
    this.headers = headers;
  }
}
