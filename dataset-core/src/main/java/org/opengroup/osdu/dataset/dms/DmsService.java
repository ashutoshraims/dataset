/*
 * Copyright 2021 Microsoft Corporation
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

package org.opengroup.osdu.dataset.dms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.opengroup.osdu.core.common.dms.model.CopyDmsRequest;
import org.opengroup.osdu.core.common.dms.model.CopyDmsResponse;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.http.HttpRequest;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.http.IHttpClient;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.dataset.model.request.GetDatasetRegistryRequest;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;


@RequiredArgsConstructor
public class DmsService implements IDmsProvider {

    public static final String NON_OK_RESPONSE_FROM_DMS_SERVICE = "Non-OK response received from DMS service: %s";
    public static final String NO_RESPONSE_BODY_FROM_DMS_SERVICE = "No response body from DMS service.";

    public static final int CONNECTION_TIMEOUT = 60000;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final DmsServiceProperties dmsServiceProperties;
    private final IHttpClient httpClient;
    private final DpsHeaders headers;

    @Override
    public StorageInstructionsResponse getStorageInstructions(String expiryTime) {
        String url = this.createUrl("/storageInstructions");
        HttpResponse result = getHttpResponse(expiryTime, url);
        try {
            return OBJECT_MAPPER.readValue(result.getBody(), StorageInstructionsResponse.class);
        } catch (JsonProcessingException e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error", e.getMessage(), e);
        }
    }

    @Override
    public RetrievalInstructionsResponse getRetrievalInstructions(GetDatasetRegistryRequest request, String expiryTime) {
        String url = this.createUrl("/retrievalInstructions");
        Map<String, String> params = new HashMap<>();
        params.put("expiryTime", expiryTime);
        HttpResponse result = getHttpResponse(request, url, params);
        try {
            return OBJECT_MAPPER.readValue(result.getBody(), RetrievalInstructionsResponse.class);
        } catch (JsonProcessingException e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error", e.getMessage(), e);
        }
    }

    @Override
    public List<CopyDmsResponse> copyDmsToPersistentStorage(CopyDmsRequest copyDmsRequest) {
        String url = this.createUrl("/copy");
        HttpResponse result = getHttpResponse(copyDmsRequest, url);
        try {
            return OBJECT_MAPPER.readValue(result.getBody(), new TypeReference<List<CopyDmsResponse>>(){});
        } catch (JsonProcessingException e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error", e.getMessage(), e);
        }
    }
    @Override
    public void revokeUrl(Map<String, String> revokeURLRequest) throws DmsException {
        String url = this.createUrl("/revokeURL");
        this.getHttpResponse(revokeURLRequest, url);
    }

    private HttpResponse getHttpResponse(Object request, String url) {
        HttpResponse result = this.httpClient.send(HttpRequest.post(request)
            .url(url)
            .headers(this.headers.getHeaders())
            .build());

        int responseCode = result.getResponseCode();

        if ((responseCode < 200 || responseCode > 299)) {
            String reason = String.format(NON_OK_RESPONSE_FROM_DMS_SERVICE, url);
            String body = result.getBody();
            String message = StringUtils.isBlank(body) ? NO_RESPONSE_BODY_FROM_DMS_SERVICE : body;
            throw new AppException(responseCode, reason, message);
        }
        return result;
    }

    private String createUrl(String path) {
        try {
            URIBuilder uriBuilder = new URIBuilder(dmsServiceProperties.getDmsServiceBaseUrl());
            uriBuilder.setPath(uriBuilder.getPath() + path);
            return uriBuilder.build().normalize().toString();
        } catch (URISyntaxException e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Invalid URL", e.getMessage(), e);
        }
    }
    private HttpResponse getHttpResponse(Object request, String url, java.util.Map<String, String> params) {
        HttpResponse result = this.httpClient.send(HttpRequest.post(request)
                .url(url)
                .headers(this.headers.getHeaders())
                .queryParams(params)
                .connectionTimeout(CONNECTION_TIMEOUT)
                .build());

        int responseCode = result.getResponseCode();

        if ((responseCode < 200 || responseCode > 299)) {
            String reason = String.format(NON_OK_RESPONSE_FROM_DMS_SERVICE, url);
            String body = result.getBody();
            String message = StringUtils.isBlank(body) ? NO_RESPONSE_BODY_FROM_DMS_SERVICE : body;
            throw new AppException(responseCode, reason, message);
        }
        return result;
    }
}
