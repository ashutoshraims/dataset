package org.opengroup.osdu.dataset.provider.interfaces;

import org.apache.commons.lang3.NotImplementedException;
import org.opengroup.osdu.dataset.model.MetadataRecordData;

import java.util.Optional;

public interface ICloudStorage {

    /**
     * Creates the placeholder container, should manage internally if the container is already present
     * @param dataPartitionId
     */
    void createStorage(String dataPartitionId);

    /***
     * Writes to an existing file on cloud, should manage internally if the file already exists, append should happen
     * @param dataPartitionId
     * @param metadataRecordData
     */
    void writeToStorage(String dataPartitionId, MetadataRecordData metadataRecordData);

    /***
     * Reads from a file, should return empty if the file not found
     * @param dataPartitionId
     * @return
     */
    String readFromStorage(String dataPartitionId);
}
