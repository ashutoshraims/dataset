package org.opengroup.osdu.dataset;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.dataset.util.LegalTagUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

public abstract class DatasetDeleteIT extends DatasetIT {

    protected static ArrayList<String> registeredDatasetRegistryIds = new ArrayList<>();

    protected static boolean runTests = false;

    public abstract void validateStorageInstructions(StorageInstructionsResponse storageInstructionsResponse);

    protected static String LEGAL_TAG = LegalTagUtils.createRandomName();

    public static void classSetup(String token) throws Exception {
        CloseableHttpResponse legalTagCreateResponse = LegalTagUtils.create(
                HeaderUtils.getHeaders(TenantUtils.getTenantName(), token),
                LEGAL_TAG);
        Assert.assertEquals(201, legalTagCreateResponse.getCode());
    }

    public static void classTearDown(String token) throws Exception {
        LegalTagUtils.delete(HeaderUtils.getHeaders(TenantUtils.getTenantName(), token), LEGAL_TAG);
    }

    @Test
    public void upload_file_register_it_and_delete_it() throws Exception {
        Assume.assumeTrue(runTests);
        String kindSubType = "dataset--File.Generic";

        //Step 1: Get Storage Instructions for File
        StorageInstructionsResponse getStorageInstResponse = storageInstructions(kindSubType);

        //Step 2: Upload File
        String fileName = "testFile.txt";
        String fileContents = "Hello World!";
        String fileSource = uploadFileToSignedUrl(getStorageInstResponse, fileName, fileContents);

        //Step 3: Register File
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String datasetRegistryId = String.format("%s:%s:%s", TenantUtils.getTenantName(), kindSubType, uuid);

        Record datasetRegistry = createDatasetRegistry(datasetRegistryId, fileName, fileSource);

        CloseableHttpResponse datasetRegistryResponse = testRegisterDatasetRequest(Collections.singletonList(datasetRegistry));
        System.out.println("Registry response " + EntityUtils.toString(datasetRegistryResponse.getEntity()));
        Assert.assertEquals(201, datasetRegistryResponse.getCode());

        registeredDatasetRegistryIds.add(datasetRegistryId);

        //Step 4: send delete metadata request
        CloseableHttpResponse deleteDatasetResponse = sendDeleteDatasetRequest(datasetRegistryId);
        Assert.assertEquals(204, deleteDatasetResponse.getCode());
    }

    private CloseableHttpResponse sendDeleteDatasetRequest(String metadataRecordId) throws Exception {
        CloseableHttpResponse deleteResponse = TestUtils.send(String.format("metadataRecord/%s/softDelete", metadataRecordId), "POST",
                HeaderUtils.getHeaders(TenantUtils.getTenantName(), testUtils.getToken()),
                "", "");
        return deleteResponse;
    }
}
