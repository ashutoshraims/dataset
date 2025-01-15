// Copyright 2017-2019, Schlumberger
// Copyright © 2021 Amazon Web Services
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.dataset;

import com.sun.jersey.api.client.Client;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class TestUtils {
    protected static String token = null;
//    protected static String noDataAccessToken = null;

    protected static String domain = System.getProperty("DOMAIN",System.getenv("DOMAIN"));
    public static final boolean EXECUTE_IT_TESTS = Boolean.parseBoolean(System.getProperty("EXECUTE_IT_TESTS",System.getenv("EXECUTE_IT_TESTS")));
    public static final String STORAGE_BASE_URL = System.getProperty("STORAGE_BASE_URL",System.getenv("STORAGE_BASE_URL"));
    public static final String LEGAL_BASE_URL = System.getProperty("LEGAL_BASE_URL",System.getenv("LEGAL_BASE_URL"));
    public static final String DATASET_BASE_URL = System.getProperty("DATASET_BASE_URL",System.getenv("DATASET_BASE_URL"));
    public static final String PROVIDER_KEY = System.getProperty("PROVIDER_KEY", System.getenv("PROVIDER_KEY"));
    private static final String SCHEMA_AUTHORITY = System.getProperty("SCHEMA_AUTHORITY",System.getenv("SCHEMA_AUTHORITY"));

    private static final String DEFAULT_SCHEMA_AUTHORITY = "osdu";

    public static final String getDomain() {
        return domain;
    }

    public static final String getProviderKey() {
        return PROVIDER_KEY;
    }

    public static String getEnvironment() {
        return System.getProperty("DEPLOY_ENV", System.getenv("DEPLOY_ENV"));
    }

    public static String getTenantName() {
        return System.getProperty("TENANT_NAME", System.getenv("TENANT_NAME"));
    }
    public static final String getSchemaAuthority() {
        if (SCHEMA_AUTHORITY == null) {
            return DEFAULT_SCHEMA_AUTHORITY;
        }

        return SCHEMA_AUTHORITY;
    }

    public static String getApiPath(String api) throws Exception {
        URL mergedURL = new URL(DATASET_BASE_URL + api);
        log.info(mergedURL.toString());
        return mergedURL.toString();
    }

    public abstract String getToken() throws Exception;

    //public abstract String getNoDataAccessToken() throws Exception;

    private static void log(String httpMethod, String url, String body) {
        log.info(String.format("%s: %s", httpMethod, url));
        log.info(body);
    }

    private static ClassicHttpRequest createHttpRequest(String path, String httpMethod, String requestBody,
                                                        Map<String, String> headers) {
        String url = path;
        ClassicRequestBuilder classicRequestBuilder;
        if(requestBody != null) {
            classicRequestBuilder = ClassicRequestBuilder.create(httpMethod)
                    .setUri(url)
                    .setEntity(requestBody, ContentType.APPLICATION_JSON);
        } else {
            classicRequestBuilder = ClassicRequestBuilder.create(httpMethod)
                    .setUri(url);
        }
        headers.forEach(classicRequestBuilder::setHeader);
        return classicRequestBuilder.build();
    }

    private static BasicHttpClientConnectionManager createBasicHttpClientConnectionManager() {
        ConnectionConfig connConfig = ConnectionConfig.custom()
                .setConnectTimeout(1500000, TimeUnit.MILLISECONDS)
                .setSocketTimeout(1500000, TimeUnit.MILLISECONDS)
                .build();
        BasicHttpClientConnectionManager cm = new BasicHttpClientConnectionManager();
        cm.setConnectionConfig(connConfig);
        return cm;
    }

    protected static Client getClient() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            log.error("Exception occurred while creating Client"+e.getMessage());
        }
        return Client.create();
    }

    public static CloseableHttpResponse send(String path, String httpMethod, Map<String, String> headers,
                                             String requestBody, String query) throws Exception {

        String apiPath = getApiPath(path + query);
        log(httpMethod, apiPath, requestBody);

        BasicHttpClientConnectionManager cm = createBasicHttpClientConnectionManager();
        ClassicHttpRequest httpRequest = createHttpRequest(apiPath, httpMethod, requestBody, headers);

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().setConnectionManager(cm).build()) {
            return httpClient.execute(httpRequest, new CustomHttpClientResponseHandler());
        }
    }

    public static CloseableHttpResponse send(String url, String path, String httpMethod, Map<String, String> headers,
                                             String requestBody) throws Exception {

        String apiPath = url + path;
        log(httpMethod, apiPath, requestBody);

        BasicHttpClientConnectionManager cm = createBasicHttpClientConnectionManager();
        ClassicHttpRequest httpRequest = createHttpRequest(apiPath, httpMethod, requestBody, headers);

        try (CloseableHttpClient httpClient = HttpClientBuilder.create().setConnectionManager(cm).build()) {
            return httpClient.execute(httpRequest, new CustomHttpClientResponseHandler());
        }
    }
}