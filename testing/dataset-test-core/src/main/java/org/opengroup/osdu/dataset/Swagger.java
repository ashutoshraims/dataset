package org.opengroup.osdu.dataset;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.http.HttpStatus;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public abstract class Swagger extends TestBase {

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

}
