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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.dms.model.DatasetRetrievalProperties;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.StorageRole;
import org.opengroup.osdu.dataset.controller.DatasetDmsController;
import org.opengroup.osdu.dataset.logging.AuditLogger;
import org.opengroup.osdu.dataset.provider.interfaces.ICloudStorage;
import org.opengroup.osdu.dataset.provider.interfaces.IDatasetDmsServiceMap;
import org.opengroup.osdu.dataset.service.DatasetDmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(controllers = DatasetDmsController.class)
@AutoConfigureMockMvc
@RunWith(Theories.class)
public class DatasetDmsControllerTest {

  public static final String EXPECTED_VALID_STORAGE = "{\"providerKey\":\"TEST\",\"storageLocation\":{}}";
  //Need to update following from storageInstructions.expiryTime to storageInstructions.arg1 since jakarta no longer return parameter name.
  public static final String EXPECTED_NOT_VALID_STORAGE = "{\"code\":400,\"reason\":\"Validation error.\",\"message\":\"storageInstructions.arg1: must match \\\"\\\\d+([mhd]|[MHD])$\\\"\"}";
  public static final String EXPECTED_VALID_RETRIEVAL = "{\"datasets\":[{\"datasetRegistryId\":\"id\",\"retrievalProperties\":{},\"providerKey\":\"TEST\"}]}";
  //Need to update following from storageInstructions.expiryTime to storageInstructions.arg1 since jakarta no longer return parameter name.
  public static final String EXPECTED_NOT_VALID_RETRIEVAL = "{\"code\":400,\"reason\":\"Validation error.\",\"message\":\"retrievalInstructions.arg1: must match \\\"\\\\d+([mhd]|[MHD])$\\\"\"}";
  @ClassRule
  public static final SpringClassRule scr = new SpringClassRule();
  @Rule
  public final SpringMethodRule smr = new SpringMethodRule();

  private MockMvc mockMvc;
  @MockBean
  private DpsHeaders headers;
  @MockBean
  private DatasetDmsService datasetDmsService;
  @MockBean
  private IDatasetDmsServiceMap serviceMap;
  @MockBean
  private AuditLogger auditLogger;
  @MockBean
  private ICache<String, Groups> cache;
  @Autowired
  private WebApplicationContext context;

  @MockBean
  private ICloudStorage cloudStorage;
  @Before
  public void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

    StorageInstructionsResponse testLocation = StorageInstructionsResponse.builder()
        .providerKey("TEST")
        .storageLocation(new HashMap<>())
        .build();
    when(datasetDmsService.getStorageInstructions(any(), any())).thenReturn(testLocation);

    RetrievalInstructionsResponse testInstructions = RetrievalInstructionsResponse.builder()
        .datasets(
            Collections.singletonList(
                DatasetRetrievalProperties.builder()
                    .providerKey("TEST")
                    .retrievalProperties(new HashMap<>())
                    .datasetRegistryId("id")
                    .build()))
        .build();
    when(datasetDmsService.getRetrievalInstructions(any(), any())).thenReturn(testInstructions);
  }

  @Test
  @Theory
  @WithMockUser(
      username = "test_user",
      roles = {StorageRole.CREATOR}
  )
  public void whenExpiryTimeValidStorageInstructionsThenReturnsOK(
      @FromDataPoints("VALID_TIME") String validTime)
      throws Exception {
    MvcResult mvcResult = postStorageInstructions(validTime);
    assertEquals("Http Status should be 200 (OK).", 200, mvcResult.getResponse().getStatus());
    assertTrue(mvcResult.getResponse().getContentAsString().contains(EXPECTED_VALID_STORAGE));
  }

  @Test
  @Theory
  @WithMockUser(
      username = "test_user",
      roles = {StorageRole.CREATOR}
  )
  public void whenNotValidExpiryTimeStorageInstructionsThenReturnsBadRequest(
      @FromDataPoints("NOT_VALID_TIME") String notValidTime) throws Exception {
    MvcResult mvcResult = postStorageInstructions(notValidTime);
    assertEquals("Http Status should be 400 (Bad Request).", 400,
        mvcResult.getResponse().getStatus());
    assertTrue(mvcResult.getResponse().getContentAsString().contains(EXPECTED_NOT_VALID_STORAGE));
  }

  @Test
  @Theory
  @WithMockUser(
      username = "test_user",
      roles = {StorageRole.CREATOR}
  )
  public void whenExpiryTimeValidGetRetrievalInstructionsThenReturnsOK(
      @FromDataPoints("VALID_TIME") String validTime)
      throws Exception {
    MvcResult mvcResult = getRetrievalInstructions(validTime);
    assertEquals("Http Status should be 200 (OK).", 200, mvcResult.getResponse().getStatus());
    assertTrue(mvcResult.getResponse().getContentAsString().contains(EXPECTED_VALID_RETRIEVAL));
  }

  @Test
  @Theory
  @WithMockUser(
      username = "test_user",
      roles = {StorageRole.CREATOR}
  )
  public void whenNotValidExpiryTimeGetRetrievalInstructionsThenReturnsBadRequest(
      @FromDataPoints("NOT_VALID_TIME") String notValidTime) throws Exception {
    MvcResult mvcResult = getRetrievalInstructions(notValidTime);
    assertEquals("Http Status should be 400 (Bad Request).", 400,
        mvcResult.getResponse().getStatus());
    assertTrue(mvcResult.getResponse().getContentAsString().contains(EXPECTED_NOT_VALID_RETRIEVAL));
  }

  @Test
  @Theory
  @WithMockUser(
      username = "test_user",
      roles = {StorageRole.CREATOR}
  )
  public void whenExpiryTimeValidPostRetrievalInstructionsThenReturnsOK(
      @FromDataPoints("VALID_TIME") String validTime)
      throws Exception {
    MvcResult mvcResult = postRetrievalInstructions(validTime);
    assertEquals("Http Status should be 200 (OK).", 200, mvcResult.getResponse().getStatus());
    assertTrue(mvcResult.getResponse().getContentAsString().contains(EXPECTED_VALID_RETRIEVAL));
  }

  @Test
  @Theory
  @WithMockUser(
      username = "test_user",
      roles = {StorageRole.CREATOR}
  )
  public void whenNotValidExpiryTimePostRetrievalInstructionsThenReturnsBadRequest(
      @FromDataPoints("NOT_VALID_TIME") String notValidTime) throws Exception {
    MvcResult mvcResult = postRetrievalInstructions(notValidTime);
    assertEquals("Http Status should be 400 (Bad Request).", 400,
        mvcResult.getResponse().getStatus());
    assertTrue(mvcResult.getResponse().getContentAsString().contains(EXPECTED_NOT_VALID_RETRIEVAL));
  }

  private MvcResult postStorageInstructions(String time) throws Exception {
    return mockMvc.perform(
            post("/storageInstructions")
                .contentType("application/json")
                .header("data-partition-id", "osdu")
                .header("Authorization", "Bearer bearer")
                .param("kindSubType", "dataset--File")
                .param("expiryTime", time)
                .with(csrf()))
        .andReturn();
  }

  private MvcResult getRetrievalInstructions(String time) throws Exception {
    return mockMvc.perform(
            get("/retrievalInstructions")
                .contentType("application/json")
                .header("data-partition-id", "osdu")
                .header("Authorization", "Bearer bearer")
                .param("id", "opendes:dataset--File.Generic:8118591ee2")
                .param("expiryTime", time)
                .with(csrf()))
        .andReturn();
  }

  private MvcResult postRetrievalInstructions(String time) throws Exception {
    MvcResult mvcResult = mockMvc.perform(
            post("/retrievalInstructions")
                .contentType("application/json")
                .header("data-partition-id", "osdu")
                .header("Authorization", "Bearer bearer")
                .param("expiryTime", time)
                .content("{\"datasetRegistryIds\": [\"{opendes:dataset--File.Generic:8118591ee2}\"]}")
                .with(csrf()))
        .andReturn();
    return mvcResult;
  }

  @DataPoints("VALID_TIME")
  public static List<String> validTimeList() {
    return ImmutableList.of(
        "4D",
        "4d",
        "24d",
        "24H",
        "24h",
        "60M",
        "60m"
    );
  }

  @DataPoints("NOT_VALID_TIME")
  public static List<String> notValidQueriesList() {
    return ImmutableList.of(
        "",
        "M",
        "NOT_VALID",
        "%$#",
        "<script>blah</script>",
        "123$H",
        "123X",
        "123",
        "1M2H3D",
        "456 D",
        "123MH",
        "12D34"
    );
  }
}
