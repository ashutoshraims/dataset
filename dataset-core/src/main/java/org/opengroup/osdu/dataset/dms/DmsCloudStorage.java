package org.opengroup.osdu.dataset.dms;

import org.apache.commons.lang3.NotImplementedException;
import org.opengroup.osdu.dataset.model.MetadataRecordData;
import org.opengroup.osdu.dataset.provider.interfaces.ICloudStorage;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//TODO: Remove this class from here once all the providers have an ICloudStorage implemented
@Repository
public class DmsCloudStorage implements ICloudStorage {
    /**
     * Creates the placeholder container, should manage internally if the container is already present
     *
     * @param dataPartitionId
     */
    @Override
    public void createStorage(String dataPartitionId) {
        throw new NotImplementedException();
    }

    /***
     * Writes to an existing file on cloud, should manage internally if the file already exists, append should happen
     * @param dataPartitionId
     * @param metadataRecordData
     */
    @Override
    public void writeToStorage(String dataPartitionId, MetadataRecordData metadataRecordData) {
        throw new NotImplementedException();
    }

    /***
     * Reads from a file, should return empty if the file not found
     * @param dataPartitionId
     * @return
     */
    @Override
    public String readFromStorage(String dataPartitionId) {
        throw new NotImplementedException();
    }
}
