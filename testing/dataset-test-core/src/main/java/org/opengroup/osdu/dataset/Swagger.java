package org.opengroup.osdu.dataset;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public abstract class Swagger extends TestBase {

    protected static final String SWAGGER_API_PATH = "swagger";
    protected static final String SWAGGER_API_DOCS_PATH = "api-docs";

    @Test
    public void shouldReturn200_whenSwaggerApiIsCalled() throws Exception {
        ClientResponse response = TestUtils
                .send(SWAGGER_API_PATH, "GET", Collections.emptyMap(), "", "");
        assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

    @Test
    public void shouldReturn200_whenSwaggerApiDocsIsCalled() throws Exception {
        ClientResponse response = TestUtils
                .send(SWAGGER_API_DOCS_PATH, "GET", Collections.emptyMap(), "", "");
        assertEquals(HttpStatus.SC_OK, response.getStatus());
    }

}
