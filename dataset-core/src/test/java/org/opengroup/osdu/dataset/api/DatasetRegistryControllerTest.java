/*
 * Copyright 2020-2023 Google LLC
 * Copyright 2020-2023 EPAM Systems, Inc
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

package org.opengroup.osdu.dataset.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.StorageRole;
import org.opengroup.osdu.dataset.logging.AuditLogger;
import org.opengroup.osdu.dataset.model.request.CreateDatasetRegistryRequest;
import org.opengroup.osdu.dataset.provider.interfaces.IDatasetDmsServiceMap;
import org.opengroup.osdu.dataset.service.DatasetRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@WebMvcTest(controllers = DatasetRegistryApi.class)
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class DatasetRegistryControllerTest {

  private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private DpsHeaders headers;

  @MockBean private DatasetRegistryService dataRegistryService;

  @MockBean private IDatasetDmsServiceMap dmsServiceMap;

  @MockBean private ICache<String, Groups> cache;

  @MockBean private AuditLogger auditLogger;

  @Autowired private WebApplicationContext context;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  @WithMockUser(
      username = "test_user",
      roles = {StorageRole.CREATOR})
  public void whenRequestEmpty_thenReturns400WithErrorMessage() throws Exception {
    MvcResult mvcResult =
        mockMvc
            .perform(
                put("/registerDataset")
                    .contentType("application/json")
                    .header("data-partition-id", "osdu")
                    .header("Authorization", "Bearer bearer")
                    .with(csrf())
                    .content(""))
            .andReturn();

    assertEquals(
        "Http Status should be 400 (Bad Request).", 400, mvcResult.getResponse().getStatus());
    assertTrue(
        mvcResult.getResponse().getContentAsString().contains("Required request body is missing."));
  }

  @Test
  @WithMockUser(
      username = "test_user",
      roles = {StorageRole.CREATOR})
  public void whenDatasetRegistriesEmpty_thenReturns400WithErrorMessage() throws Exception {
    String request = new Gson().toJson(new CreateDatasetRegistryRequest());
    MvcResult mvcResult =
        mockMvc
            .perform(
                put("/registerDataset")
                    .contentType("application/json")
                    .header("data-partition-id", "osdu")
                    .header("Authorization", "Bearer bearer")
                    .with(csrf())
                    .content(request))
            .andReturn();

    assertEquals(
        "Http Status should be 400 (Bad Request).", 400, mvcResult.getResponse().getStatus());
    assertTrue(
        mvcResult.getResponse().getContentAsString().contains("datasetRegistries cannot be empty"));
  }
}
