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

import static org.junit.Assert.*;

import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.http.IHttpClient;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.dataset.dms.DmsServiceProperties;
import org.opengroup.osdu.dataset.model.response.GetDatasetRetrievalInstructionsResponse;

@RunWith(MockitoJUnitRunner.class)
public class AzureDmsServiceTest {

  @Mock
  private DmsServiceProperties dmsServiceProperties;

  @Mock
  private IHttpClient httpClient;

  @Mock
  private DpsHeaders headers;

  @InjectMocks
  private AzureDmsService restService;

  @Test
  public void should_successfully_get_retrieval_instructions() {
    String url = "https://host/api/dms/eds/v1";
    HttpResponse httpResponse = new HttpResponse();
    String body = "{\"datasets\" : []}";
    httpResponse.setBody(body);
    RetrievalInstructionsResponse retrievalInstructionsResponse = new RetrievalInstructionsResponse(new ArrayList<>());

    Mockito.when(this.dmsServiceProperties.getDmsServiceBaseUrl()).thenReturn(url);
    Mockito.when(this.httpClient.send(Mockito.any())).thenReturn(httpResponse);

    assertEquals(retrievalInstructionsResponse, this.restService.getRetrievalInstructions(null));
  }

  @Test(expected = AppException.class)
  public void should_throw_exception_when_response_is_not_valid() {
    String url = "https://host/api/dms/eds/v1";
    HttpResponse httpResponse = new HttpResponse();
    String body = "{\"datasets\" : {}}";
    httpResponse.setBody(body);
    RetrievalInstructionsResponse retrievalInstructionsResponse = new RetrievalInstructionsResponse(new ArrayList<>());

    Mockito.when(this.dmsServiceProperties.getDmsServiceBaseUrl()).thenReturn(url);
    Mockito.when(this.httpClient.send(Mockito.any())).thenReturn(httpResponse);

    this.restService.getRetrievalInstructions(null);
  }

  @Test(expected = AppException.class)
  public void should_throw_exception_when_url_is_not_valid() {
    String url = "#:/$&#";
    Mockito.when(this.dmsServiceProperties.getDmsServiceBaseUrl()).thenReturn(url);

    this.restService.getRetrievalInstructions(null);
  }
}
