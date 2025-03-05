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

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.http.HttpStatus;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public final class SwaggerTest extends TestBase {

    protected static final String SWAGGER_API_PATH = "swagger";
    protected static final String SWAGGER_API_DOCS_PATH = "api-docs";

    @Test
    public void shouldReturn200_whenSwaggerApiIsCalled() throws Exception {
        CloseableHttpResponse response = TestUtils
                .send(SWAGGER_API_PATH, "GET", Collections.emptyMap(), "", "");
        assertEquals(HttpStatus.SC_OK, response.getCode());
    }

    @Test
    public void shouldReturn200_whenSwaggerApiDocsIsCalled() throws Exception {
        CloseableHttpResponse response = TestUtils
                .send(SWAGGER_API_DOCS_PATH, "GET", Collections.emptyMap(), "", "");
        assertEquals(HttpStatus.SC_OK, response.getCode());
    }

    @Override
    public void setup() throws Exception {

    }

    @Override
    public void tearDown() throws Exception {

    }
}
