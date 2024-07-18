package org.opengroup.osdu.dataset.provider.azure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.dataset.model.MetadataRecordData;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CloudStorageImplTest{

    @Mock
    BlobStore blobStore;
    private String containerName = "to-be-purged";
    private String blobName = "record.json";

    private String dataPartition = "data-partition";

    @Mock
    private JaxRsDpsLog logger;

    @InjectMocks
    CloudStorageImpl sut;


    @Test
    public void createStorage_should_doNothing_when_containerExists() {
        AppException appException = new AppException(409, "ContainerAlreadyExists", "ContainerAlreadyExists");
        doThrow(appException).when(blobStore).createBlobContainer(dataPartition, containerName);

        sut.createStorage(dataPartition);

        verify(blobStore, times(1)).createBlobContainer(dataPartition, containerName);
    }
    @Test
    public void createStorage_shouldThrowAppException_whenBlobStoreThrowsException() {
        AppException appException = new AppException(500, "someOtherError", "some other error");
        doThrow(appException).when(blobStore).createBlobContainer(dataPartition, containerName);

        Exception exception = assertThrows(Exception.class, ()-> {
            sut.createStorage(dataPartition);
        });

        assertEquals(appException, exception);
    }

    @Test
    public void writeToStorage_shouldAttemptReadAndCreateBlob_whenBlobNotFound() throws JsonProcessingException {
        AppException appException = new AppException(404, "Blob Not Found", "Blob Not Found");
        doThrow(appException).when(blobStore).readFromStorageContainer(dataPartition, blobName, containerName);

        MetadataRecordData metadataRecordData = new MetadataRecordData("RECORD_ID", "2024-07-15 13:46:55");
        String data = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(List.of(metadataRecordData));

        sut.writeToStorage(dataPartition, metadataRecordData);

        verify(blobStore, times(1)).writeToStorageContainer(dataPartition, blobName, data, containerName);
        verify(blobStore, times(1)).readFromStorageContainer(dataPartition, blobName, containerName);
    }

    @Test
    public void writeToStorage_shouldAttemptReadAndAppendToBlob_whenBlobIsFound() throws JsonProcessingException {
        MetadataRecordData existingMetadataRecordData = new MetadataRecordData("RECORD_ID", "2024-07-12 13:46:55");
        String oldData = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(List.of(existingMetadataRecordData));

        when(blobStore.readFromStorageContainer(dataPartition, blobName, containerName)).thenReturn(oldData);

        MetadataRecordData metadataRecordData = new MetadataRecordData("RECORD_ID", "2024-07-15 13:46:55");
        String data = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(List.of(existingMetadataRecordData, metadataRecordData));

        sut.writeToStorage(dataPartition, metadataRecordData);

        verify(blobStore, times(1)).writeToStorageContainer(dataPartition, blobName, data, containerName);
        verify(blobStore, times(1)).readFromStorageContainer(dataPartition, blobName, containerName);
    }

    @Test
    public void writeToStorage_shouldAttemptReadAndAppend_whenEmptyBlobFound() throws JsonProcessingException {
        when(blobStore.readFromStorageContainer(dataPartition, blobName, containerName)).thenReturn("");

        MetadataRecordData metadataRecordData = new MetadataRecordData("RECORD_ID", "2024-07-15 13:46:55");
        String data = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(List.of(metadataRecordData));

        sut.writeToStorage(dataPartition, metadataRecordData);

        verify(blobStore, times(1)).writeToStorageContainer(dataPartition, blobName, data, containerName);
        verify(blobStore, times(1)).readFromStorageContainer(dataPartition, blobName, containerName);
    }
}