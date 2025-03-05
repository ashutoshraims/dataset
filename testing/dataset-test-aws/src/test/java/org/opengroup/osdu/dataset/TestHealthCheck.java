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

package org.opengroup.osdu.dataset;

import static org.junit.Assert.assertEquals;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class TestHealthCheck extends HealthCheckApiTest {

  private final AWSTestUtils utils = new AWSTestUtils();
  @Override
  public void setup() throws Exception {}

  private void verify_livenessCheckWorksWithToken(String token) throws Exception {
    CloseableHttpResponse response =
        TestUtils.send(
            "liveness_check",
            "GET",
            HeaderUtils.getHeaders(TenantUtils.getTenantName(), token),
            "",
            "");
    assertEquals(HttpStatus.OK.value(), response.getCode());
  }

  @Test
  @Override
  public void should_returnOk() throws Exception {
    verify_livenessCheckWorksWithToken(utils.getToken());
  }

  @Test
  public void should_allowNoAccessUserToCheckForLiveness() throws Exception {
    verify_livenessCheckWorksWithToken(utils.getNoDataAccessToken());
  }

  @Override
  public void tearDown() throws Exception {}
}
