package org.opengroup.osdu.dataset.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.dms.model.RetrievalInstructionsResponse;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.dataset.dms.DmsException;
import org.opengroup.osdu.dataset.dms.DmsServiceProperties;
import org.opengroup.osdu.dataset.dms.IDmsFactory;
import org.opengroup.osdu.dataset.dms.IDmsProvider;
import org.opengroup.osdu.dataset.model.request.GetDatasetRegistryRequest;
import org.opengroup.osdu.dataset.provider.interfaces.IDatasetDmsServiceMap;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


@RunWith(MockitoJUnitRunner.class)
public class DatasetDmsServiceImplTest {


    @Mock
    private DpsHeaders headers;

    @Mock
    private IDmsFactory dmsFactory;

    @Mock
    private IDatasetDmsServiceMap dmsServiceMap;

    @Mock
    IDmsProvider dmsProvider;

    @Mock
    DmsServiceProperties dmsServiceProperties;

    @InjectMocks
    DatasetDmsServiceImpl datasetDmsService;

    private final String RECORD_ID = "opendes:dataset--file:data";
    private final String INVALID_RECORD_ID = "closedes:dataset--file:data";
    private final String DATA_PARTITION_ID = "opendes";
    private final String KIND = "dataset--file";
    private final String INVALID_KIND = "dataset---file";
    private final String KIND_TYPE_2 = "dataset--file.*";

    Map<String, DmsServiceProperties> kindSubTypeToDmsServiceMap;
    List<String> datasetRegistryIds;

    @Before
    public void setup() {
        initMocks(this);
        kindSubTypeToDmsServiceMap = new HashMap<>();
        kindSubTypeToDmsServiceMap.put(KIND, dmsServiceProperties);
        when(headers.getPartitionId()).thenReturn(DATA_PARTITION_ID);
    }

    @Test
    public void testGetStorageInstructionsIfSubTypePresent() throws DmsException {
        testGetStorageInstructions();
    }

    @Test
    public void testGetStorageInstructionsIfSubtypePresent() throws Exception {

        addKindType2InMap();
        testGetStorageInstructions();
        removeKindType2InMap();
    }


    @Test(expected = AppException.class)
    public void testGetStorageInstructionsIfDmsServiceIsNull() {
        when(dmsServiceMap.getResourceTypeToDmsServiceMap()).thenReturn(kindSubTypeToDmsServiceMap);
        StorageInstructionsResponse actualResponse = datasetDmsService.getStorageInstructions(INVALID_KIND, null);
    }

    @Test(expected = AppException.class)
    public void testGetStorageInstructionsIfAllowStorageIsNull() {
        when(dmsServiceMap.getResourceTypeToDmsServiceMap()).thenReturn(kindSubTypeToDmsServiceMap);
        when(dmsServiceProperties.isAllowStorage()).thenReturn(false);
        StorageInstructionsResponse actualResponse = datasetDmsService.getStorageInstructions(KIND, null);
    }

    @Test
    public void testGetDatasetRetrievalInstructions() throws Exception {

        datasetRegistryIds = Collections.singletonList(RECORD_ID);
        GetDatasetRegistryRequest request = mock(GetDatasetRegistryRequest.class);
        RetrievalInstructionsResponse entryResponse = new RetrievalInstructionsResponse(new ArrayList<>());
        injectWhenClauseForDmsServiceMapAndDmsFactory();
        when(dmsProvider.getRetrievalInstructions(any(), any())).thenReturn(entryResponse);
        RetrievalInstructionsResponse actualResponse = datasetDmsService.getRetrievalInstructions(datasetRegistryIds, null);
        verifydmsServiceMapAndDmsFactory();
    }

    @Test
    public void testGetRetrievalInstructions() throws Exception {
        datasetRegistryIds = Collections.singletonList(RECORD_ID);
        testRetrievalInstructions();
    }

    @Test
    public void testGetRetrievalInstructionsWithKindType2() throws Exception {

        addKindType2InMap();
        datasetRegistryIds = Collections.singletonList(RECORD_ID);
        testRetrievalInstructions();
        removeKindType2InMap();

    }

    @Test(expected = AppException.class)
    public void testGetRetrievalInstructionsWithInvalidRecordID() throws Exception {

        datasetRegistryIds = Collections.singletonList(INVALID_RECORD_ID);
        testRetrievalInstructions();
    }

    private void addKindType2InMap() {
        kindSubTypeToDmsServiceMap.remove(KIND);
        kindSubTypeToDmsServiceMap.put(KIND_TYPE_2, dmsServiceProperties);
    }

    private void removeKindType2InMap() {
        kindSubTypeToDmsServiceMap.remove(KIND_TYPE_2);
    }

    private void testGetStorageInstructions() throws DmsException {
        injectWhenClauseForDmsServiceMapAndDmsFactory();
        when(dmsServiceProperties.isAllowStorage()).thenReturn(true);
        StorageInstructionsResponse actualResponse = datasetDmsService.getStorageInstructions(KIND, null);
        StorageInstructionsResponse expectedResponse = new StorageInstructionsResponse();
        verifydmsServiceMapAndDmsFactory();
        verify(dmsProvider, times(1)).getStorageInstructions(null);
    }

    private void testRetrievalInstructions() throws Exception {
        RetrievalInstructionsResponse entryResponse = new RetrievalInstructionsResponse();
        injectWhenClauseForDmsServiceMapAndDmsFactory();
        when(dmsProvider.getRetrievalInstructions(any(), any())).thenReturn(entryResponse);
        RetrievalInstructionsResponse actualResponse = datasetDmsService.getRetrievalInstructions(datasetRegistryIds, null);
        verifydmsServiceMapAndDmsFactory();
        verify(dmsProvider, times(1)).getRetrievalInstructions(any(), any());
    }

    private void verifydmsServiceMapAndDmsFactory() {
        verify(dmsServiceMap,times(1)).getResourceTypeToDmsServiceMap();
        verify(dmsFactory, times(1)).create(headers,dmsServiceProperties);
    }

    private void injectWhenClauseForDmsServiceMapAndDmsFactory() {
        when(dmsServiceMap.getResourceTypeToDmsServiceMap()).thenReturn(kindSubTypeToDmsServiceMap);
        when(dmsFactory.create(headers,dmsServiceProperties)).thenReturn(dmsProvider);
    }
}