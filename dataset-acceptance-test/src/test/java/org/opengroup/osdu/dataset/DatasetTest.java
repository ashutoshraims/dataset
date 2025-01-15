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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.http.HttpStatus;
import org.junit.*;
import org.opengroup.osdu.dataset.model.configuration.*;
import org.opengroup.osdu.dataset.model.request.IntTestGetDatasetRegistryRequest;
import org.opengroup.osdu.dataset.model.response.IntTestDatasetRetrievalDeliveryItem;
import org.opengroup.osdu.dataset.model.response.IntTestGetCreateUpdateDatasetRegistryResponse;
import org.opengroup.osdu.dataset.model.response.IntTestGetDatasetRetrievalInstructionsResponse;
import org.opengroup.osdu.dataset.model.response.IntTestGetDatasetStorageInstructionsResponse;
import org.opengroup.osdu.dataset.model.shared.DatasetConfiguration;
import org.opengroup.osdu.dataset.util.TokenTestUtils;
import org.opengroup.osdu.dataset.util.CloudStorageUtil;
import org.opengroup.osdu.dataset.util.CloudStorageUtilAnthos;
import org.opengroup.osdu.dataset.util.FileUtils;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
public final class DatasetTest extends TestBase {

    public static final String INPUT_DATASET_FILE_JSON = "input/datasetFile.json";
    private final ObjectMapper jsonMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    private static CloudStorageUtil cloudStorageUtil = null;
    private static TestUtils datasetTestUtils = new TokenTestUtils();
    private static ArrayList<String> registeredDatasetRegistryIds = new ArrayList<>();
    private static final VersionInfoUtils VERSION_INFO_UTILS = new VersionInfoUtils();
    private static final ObjectMapper objectMapper = MapperConfig.getObjectMapper();

    @BeforeClass
    public static void classSetup() throws Exception {
        cloudStorageUtil = new CloudStorageUtilAnthos();
        DatasetConfiguration.datasetSetup(datasetTestUtils.getToken());
    }

    @AfterClass
    public static void classTearDown() throws Exception {
        classTearDown(datasetTestUtils.getToken());
    }

    @Before
    @Override
    public void setup() {
        testUtils = new TokenTestUtils();
    }

    @After
    @Override
    public void tearDown() {
        testUtils = null;
    }

    public static void classTearDown(String token) throws Exception {

        for (String datasetRegistryId : registeredDatasetRegistryIds) {
            log.info("Deleting Dataset Registry: {}", datasetRegistryId);
            CloseableHttpResponse storageResponse = TestUtils.send(TestUtils.STORAGE_BASE_URL, String.format("records/%s", datasetRegistryId), "DELETE", HeaderUtils.getHeaders(TenantUtils.getTenantName(), token), "");
            log.info("Deleting Dataset Registry Response Code: {}", storageResponse.getCode());
        }
    }

    @Test
    public void should_getUploadLocation() throws Exception {
        CloseableHttpResponse response = TestUtils.send("storageInstructions", "POST", HeaderUtils.getHeaders(TenantUtils.getTenantName(), testUtils.getToken()), "", "?kindSubType=dataset--File.Generic");
        Assert.assertEquals(200, response.getCode());

        String respStr = EntityUtils.toString(response.getEntity());

        IntTestGetDatasetStorageInstructionsResponse<Object> resp = jsonMapper.readValue(respStr, IntTestGetDatasetStorageInstructionsResponse.class);

        Assert.assertEquals(TestUtils.getProviderKey(), resp.getProviderKey());

        Assert.assertNotNull(resp.getStorageLocation());
        validate_storageLocation(resp.getStorageLocation());
    }

    @Test
    public void upload_file_register_it_and_retrieve_it() throws Exception {
        String kindSubType = "?kindSubType=dataset--File." + TestConfig.getDatasetKindSubType();

        //Step 1: Get Storage Instructions for File
        IntTestGetDatasetStorageInstructionsResponse datasetInstructions = getDatasetInstructions(kindSubType);

        //Step 2: Upload File
        String fileName = "testFile.txt";
        String fileContents = "Hello World!";
        String fileSource = cloudStorageUtil.uploadCloudFileUsingProvidedCredentials(fileName, datasetInstructions.getStorageLocation(), fileContents);

        //Step 3: Register File
        String datasetRegistry = createDatasetRegistry(INPUT_DATASET_FILE_JSON, fileSource);
        String recordId = registerDataset(datasetRegistry);

        //Step 4: Retrieve File and validate contents
        IntTestDatasetRetrievalDeliveryItem datasetRetrievalItem = getDatasetRetrievalItem(recordId);

        validate_dataset_retrieval_delivery_item(datasetRetrievalItem);

        String downloadedContent = cloudStorageUtil.downloadCloudFileUsingDeliveryItem(datasetRetrievalItem.getRetrievalProperties());

        Assert.assertEquals(fileContents, downloadedContent);
    }

    private String registerDataset(String datasetRegistry) throws Exception {
        CloseableHttpResponse datasetRegistryResponse = TestUtils.send(TestUtils.DATASET_BASE_URL, "registerDataset", "PUT", HeaderUtils.getHeaders(TenantUtils.getTenantName(), testUtils.getToken()), datasetRegistry);

        Assert.assertEquals(201, datasetRegistryResponse.getCode());

        IntTestGetCreateUpdateDatasetRegistryResponse registryResponse = objectMapper.readValue(EntityUtils.toString(datasetRegistryResponse.getEntity()), IntTestGetCreateUpdateDatasetRegistryResponse.class);
        String recordId = registryResponse.getDatasetRegistries().get(0).getId();

        registeredDatasetRegistryIds.add(recordId);
        return recordId;
    }

    private IntTestGetDatasetStorageInstructionsResponse getDatasetInstructions(String dataset) throws Exception {
        CloseableHttpResponse response = TestUtils.send("storageInstructions", "POST", HeaderUtils.getHeaders(TenantUtils.getTenantName(), testUtils.getToken()), "", dataset);
        Assert.assertEquals(200, response.getCode());

        String respStr = EntityUtils.toString(response.getEntity());

        IntTestGetDatasetStorageInstructionsResponse<IntTestFileInstructionsItem> resp = jsonMapper.readValue(respStr, IntTestGetDatasetStorageInstructionsResponse.class);

        Assert.assertEquals(TestUtils.getProviderKey(), resp.getProviderKey());
        Assert.assertNotNull(resp.getStorageLocation());

        return resp;
    }

    private IntTestDatasetRetrievalDeliveryItem getDatasetRetrievalItem(String recordId) throws Exception {
        IntTestGetDatasetRegistryRequest getDatasetRequest = new IntTestGetDatasetRegistryRequest(new ArrayList<>());
        getDatasetRequest.getDatasetRegistryIds().add(recordId);

        CloseableHttpResponse retrievalClientResponse = TestUtils.send("retrievalInstructions", "POST", HeaderUtils.getHeaders(TenantUtils.getTenantName(), testUtils.getToken()), jsonMapper.writeValueAsString(getDatasetRequest), "");

        Assert.assertEquals(200, retrievalClientResponse.getCode());

        String getRetrievalRespStr = EntityUtils.toString(retrievalClientResponse.getEntity());

        IntTestGetDatasetRetrievalInstructionsResponse getRetrievalInstResponse = jsonMapper.readValue(getRetrievalRespStr, IntTestGetDatasetRetrievalInstructionsResponse.class);

        return getRetrievalInstResponse.getDatasets().get(0);
    }

    public void validate_dataset_retrieval_delivery_item(IntTestDatasetRetrievalDeliveryItem deliveryItem) {
        IntTestFileInstructionsItem fileInstructionsItem = objectMapper.convertValue(deliveryItem.getRetrievalProperties(), IntTestFileInstructionsItem.class);

        Assert.assertNotNull(fileInstructionsItem);
    }

    public void validate_storageLocation(Object storageLocation) {
        IntTestFileInstructionsItem fileInstructionsItem = objectMapper.convertValue(storageLocation, IntTestFileInstructionsItem.class);

        Assert.assertNotNull(fileInstructionsItem.getFileSource());
        Assert.assertNotNull(fileInstructionsItem.getSignedUrl());
        Assert.assertNotNull(fileInstructionsItem.getCreatedBy());
    }

    private String createDatasetRegistry(String filename, String filepath) throws IOException {
        String datasetRegistry = FileUtils.readFileFromResources(filename);
        StringSubstitutor stringSubstitutor = new StringSubstitutor(ImmutableMap.of("tenant", TenantUtils.getTenantName(), "domain", TokenTestUtils.getDomain(), "kind-subtype", TestConfig.getDatasetKindSubType(), "filepath", filepath, "legal-tag", TestConfig.getLegalTag()));
        return stringSubstitutor.replace(datasetRegistry);
    }

    @Test
    public void should_returnInfo() throws Exception {
        CloseableHttpResponse response = TestUtils.send("info", "GET", HeaderUtils.getHeaders(TenantUtils.getTenantName(), testUtils.getToken()), "", "");

        assertEquals(HttpStatus.SC_OK, response.getCode());

        VersionInfoUtils.VersionInfo responseObject = VERSION_INFO_UTILS.getVersionInfoFromResponse(response);

        assertNotNull(responseObject.groupId);
        assertNotNull(responseObject.artifactId);
        assertNotNull(responseObject.version);
        assertNotNull(responseObject.buildTime);
        assertNotNull(responseObject.branch);
        assertNotNull(responseObject.commitId);
        assertNotNull(responseObject.commitMessage);
    }
}