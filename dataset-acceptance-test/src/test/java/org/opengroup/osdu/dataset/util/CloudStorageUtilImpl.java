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

package org.opengroup.osdu.dataset.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.dataset.model.configuration.IntTestFileInstructionsItem;
import org.opengroup.osdu.dataset.model.configuration.MapperConfig;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Slf4j
public class CloudStorageUtilImpl extends CloudStorageUtil {

    private final ObjectMapper objectMapper;

    public CloudStorageUtilImpl() {
        objectMapper = MapperConfig.getObjectMapper();
    }

    public String uploadCloudFileUsingProvidedCredentials(String fileName, Object storageLocationProperties,
        String fileContents) {
        IntTestFileInstructionsItem fileInstructionsItem = objectMapper
            .convertValue(storageLocationProperties, IntTestFileInstructionsItem.class);

        Client client = TokenTestUtils.getClient();

        try {
            WebResource resource = client.resource(fileInstructionsItem.getSignedUrl().toURI());
            Builder builder = resource.accept(MediaType.APPLICATION_JSON).type(MediaType.TEXT_PLAIN);
            ClientResponse put = builder.method(HttpMethod.PUT, ClientResponse.class, fileContents);
        } catch (Exception e) {
            log.error("Upload file by signed URL FAIL", e);
        }
        return fileInstructionsItem.getFileSource();
    }

    public String downloadCloudFileUsingDeliveryItem(Object deliveryItem) {
        IntTestFileInstructionsItem fileInstructionsItem = objectMapper
            .convertValue(deliveryItem, IntTestFileInstructionsItem.class);
        try {
            return FileUtils.readFileFromUrl(fileInstructionsItem.getSignedUrl());
        } catch (IOException e) {
            log.error( "Download file by signed URL FAIL", e);
        }
        return null;
    }
}
