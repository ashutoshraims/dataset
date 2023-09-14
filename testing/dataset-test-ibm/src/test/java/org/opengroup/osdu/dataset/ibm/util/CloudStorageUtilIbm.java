/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.dataset.ibm.util;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.opengroup.osdu.dataset.CloudStorageUtil;
import org.opengroup.osdu.dataset.TestUtils;

import java.util.HashMap;
import java.util.Map;

public class CloudStorageUtilIbm extends CloudStorageUtil {

    @Override
    public String uploadCloudFileUsingProvidedCredentials(String fileName, Object storageLocationProperties,
                                                          String fileContents) throws Exception {
        // Upload File to Signed URL
        Map<String, String> fileUploadHeaders = new HashMap<>();
        fileUploadHeaders.put("x-ms-blob-type", "BlockBlob");

        Map<String, Object> storageLocation = (Map<String, Object>) storageLocationProperties;
        CloseableHttpResponse fileUploadResponse = TestUtils.send(
                storageLocation.get("signedUrl").toString(), "",
                "PUT", fileUploadHeaders, fileContents, null);

        return storageLocation.get("fileSource").toString();
    }

    @Override
    public String downloadCloudFileUsingDeliveryItem(Object retrievalLocationProperties) throws Exception {

        Map<String, Object> retrievalProperties = (Map<String, Object>) retrievalLocationProperties;
        CloseableHttpResponse fileUploadResponse = TestUtils.send(
                retrievalProperties.get("signedUrl").toString(), "",
                "GET", new HashMap<>(), "", null);
        String downloadedFileResp = null;
        downloadedFileResp = EntityUtils.toString(fileUploadResponse.getEntity());
        return downloadedFileResp;
    }

    @Override
    public void deleteCloudFile(String unsignedUrl) {
    	
    }
}