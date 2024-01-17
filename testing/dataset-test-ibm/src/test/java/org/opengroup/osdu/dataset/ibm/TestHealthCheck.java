/*
 Copyright 2002-2023 Google LLC
 Copyright 2002-2023 EPAM Systems, Inc

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

package org.opengroup.osdu.dataset.ibm;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.Assert;
import org.junit.Test;
import org.opengroup.osdu.dataset.HeaderUtils;
import org.opengroup.osdu.dataset.HealthCheckApiTest;
import org.opengroup.osdu.dataset.TenantUtils;
import org.opengroup.osdu.dataset.TestUtils;
import org.opengroup.osdu.dataset.ibm.util.IBMTestUtils;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestHealthCheck extends HealthCheckApiTest {
  @Override
  public void setup() throws Exception {}

  @Override
  public void tearDown() throws Exception {}
  private static final IBMTestUtils ibmTestUtils = new IBMTestUtils();
  @Test
  @Override
  public void should_returnOk() throws Exception {
    CloseableHttpResponse response = TestUtils.send("liveness_check", "GET", getHeaders(TenantUtils.getTenantName(), ibmTestUtils.getToken()), "", "");
    Assert.assertEquals((long) HttpStatus.OK.value(), (long)response.getCode());
  }
  public static Map<String, String> getHeaders(String tenantName, String token) {
    Map<String, String> headers = new HashMap();
    if (tenantName == null || tenantName.isEmpty()) {
      tenantName = TenantUtils.getTenantName();
    }

    headers.put("data-partition-id", tenantName);
    headers.put("Authorization", token);
    String correlationId = UUID.randomUUID().toString();
    System.out.printf("Using correlation-id for the request: %s \n", correlationId);
    headers.put("correlation-id", correlationId);
    return headers;
  }
}
