package org.opengroup.osdu.dataset.provider.azure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import org.opengroup.osdu.azure.blobstorage.BlobStore;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.dataset.model.MetadataRecordData;
import org.opengroup.osdu.dataset.provider.interfaces.ICloudStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Repository
@Primary
public class CloudStorageImpl implements ICloudStorage {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private BlobStore blobStore;
    private String containerName = "to-be-purged";
    private String blobName = "record.json";
    private String filePath = String.valueOf(Paths.get(blobName));
    @Inject
    private JaxRsDpsLog logger;


    @Override
    public void createStorage(String dataPartitionId) {
        try {
            blobStore.createBlobContainer(dataPartitionId, containerName);
            this.logger.info(String.format("Storage container %s created for %s", containerName, dataPartitionId));
        }
        catch(Exception e){
            if(e.getMessage().contains("ContainerAlreadyExists")){
                this.logger.info(String.format("Storage container %s already exists in %s", containerName, dataPartitionId));
            }
            else
                throw e;
        }
    }


    @Override
    public void writeToStorage(String dataPartitionId, MetadataRecordData newRecord) {
        try{
            String updatedContent = getUpdatedContent(dataPartitionId, newRecord);
            blobStore.writeToStorageContainer(dataPartitionId, filePath, updatedContent, containerName);

        } catch (JsonProcessingException e) {
            this.logger.error("Error occurred during JSON serialization", e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "Json Serialization Exception during writing purge details");
        }
    }

    private String getUpdatedContent(String dataPartitionId, MetadataRecordData newRecord) throws JsonProcessingException {
        String existingContentOptional = readFromStorage(dataPartitionId);
        List<MetadataRecordData> metadataRecords = new ArrayList<>();
        if (!existingContentOptional.isEmpty()) {
            metadataRecords = OBJECT_MAPPER.readValue(existingContentOptional, new TypeReference<>() {});
        }

        metadataRecords.add(newRecord);

        return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(metadataRecords);
    }

    @Override
    public String readFromStorage(String dataPartitionId) {
        try{
            return blobStore.readFromStorageContainer(dataPartitionId, filePath, containerName);
        } catch (AppException exception) {
            if(exception.getError().getCode() == 404)
                return "";
            else
                this.logger.error("Error during read from storage");
                throw exception;
        }
    }
}
