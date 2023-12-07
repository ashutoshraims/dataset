/**
* Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*      http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.opengroup.osdu.dataset.provider.aws.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.junit.Test;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelperV2;
import org.opengroup.osdu.core.aws.dynamodb.IDynamoDBQueryHelperFactory;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.dataset.dms.DmsServiceProperties;
import org.opengroup.osdu.dataset.provider.aws.cache.DmsRegistrationCache;
import org.opengroup.osdu.dataset.provider.aws.config.ProviderConfigurationBag;
import org.opengroup.osdu.dataset.provider.aws.model.DmsRegistrations;
import org.opengroup.osdu.dataset.provider.aws.model.DynamoDmsRegistration;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class DatasetDmsServiceMapImplTest {

    private final ArrayList<DynamoDmsRegistration> registrations = new ArrayList<>();

    private final String[] datasetKinds = { "dataset:kind:1", "dataset:kind:2" };

    private final String[] dmsApiBases = { "https://base1", "https://base2" };

    private final String[] dmsApiRoutes = { "/route1", "/route2" };

    @Mock
    private DmsRegistrationCache cache;

    @Mock
    private JaxRsDpsLog logger;

    private final ProviderConfigurationBag configBag = new ProviderConfigurationBag();

    private final DpsHeaders headers = new DpsHeaders();

    @Mock
    private IDynamoDBQueryHelperFactory queryHelperFactory;

    @Mock
    private DynamoDBQueryHelperV2 queryHelper;

    private String expectedKey;

    @Captor
    private ArgumentCaptor<DmsRegistrations> registrationCaptor;

    @Mock
    private DmsRegistrations registrationsMock;

    public DatasetDmsServiceMapImplTest() {
    }

    @Before
    public void setup() {
        for (int i = 0; i < datasetKinds.length; ++i) {
            DynamoDmsRegistration mock = Mockito.mock(DynamoDmsRegistration.class);
            when(mock.getApiBase()).thenReturn(dmsApiBases[i]);
            when(mock.getDatasetKind()).thenReturn(datasetKinds[i]);
            when(mock.getRoute()).thenReturn(dmsApiRoutes[i]);
            registrations.add(mock);
        }

        when(queryHelperFactory.getQueryHelperUsingSSM(anyString(), anyString())).thenReturn(queryHelper);

        configBag.ssmParameterPrefix = "some-prefix";
        configBag.dmsRegistrationTableRelativePath = "some-relative";

        headers.put("data-partition-id", "osdu-partition");
        headers.put("authorization", "some-authorization");

        expectedKey = DmsRegistrationCache.getCacheKey(headers);
    }

    private void runWithCacheMissNoError(String configBaseApi) {
        configBag.dmsApiBase = configBaseApi;

        DatasetDmsServiceMapImpl serviceMap = new DatasetDmsServiceMapImpl(headers, configBag, cache, queryHelperFactory, logger);

        when(cache.get(expectedKey)).thenReturn(null);

        when(queryHelper.scanTable(DynamoDmsRegistration.class)).thenReturn(registrations);

        doNothing().when(cache).put(eq(expectedKey), any());
        doNothing().when(logger).info(anyString());

        Map<String, DmsServiceProperties> result = serviceMap.getResourceTypeToDmsServiceMap();

        verify(cache, times(1)).get(expectedKey);
        verify(cache, times(1)).put(eq(expectedKey), registrationCaptor.capture());
        assertEquals(result, registrationCaptor.getValue().getDynamoDmsRegistrations());

        verify(logger, times(1)).info(anyString());

        assertEquals(datasetKinds.length, result.size());
        for (int i = 0; i < datasetKinds.length; ++i) {
            String datasetKind = datasetKinds[i];
            String apiBase = StringUtils.isEmpty(configBaseApi) ? dmsApiBases[i] : configBaseApi;
            String apiUrl = StringUtils.join(apiBase, dmsApiRoutes[i]);
            DynamoDmsRegistration mock = Mockito.mock(DynamoDmsRegistration.class);
            assertTrue(result.containsKey(datasetKind), "Expect result to contain key: " + datasetKind);
            assertEquals(apiUrl, result.get(datasetKind).getDmsServiceBaseUrl());
        }
    }

    @Test
    public void should_returnDmsServicePropsMap_when_getResourceTypeToDmsServiceMapCalledWithDefaultPath() {
        runWithCacheMissNoError("https://base-url");
    }

    @Test
    public void should_returnDmsServicePropsMap_when_getResourceTypeToDmsServiceMapCalledWithNoDefaultPath() {
        runWithCacheMissNoError("");
    }

    @Test
    public void should_throwAppException_when_getResourceTypeToDmsServiceMapCalledAndErrors() {
        DatasetDmsServiceMapImpl serviceMap = new DatasetDmsServiceMapImpl(headers, configBag, cache, queryHelperFactory, logger);

        when(cache.get(expectedKey)).thenReturn(null);
        when(queryHelper.scanTable(DynamoDmsRegistration.class)).thenThrow(RuntimeException.class);
        doNothing().when(logger).error(anyString(), any(Exception.class));

        try {
            serviceMap.getResourceTypeToDmsServiceMap();
            fail("Expect an AppException to be raised when something errors while processing a cache miss.");
        } catch (AppException e) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getError().getCode());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), e.getError().getReason());

            verify(logger, times(1)).error(anyString(), any(Exception.class));
        }
    }

    @Test
    public void should_returnDmsServicePropsMap_when_getResourceTypeToDmsServiceMapCalledWithCacheHit() {
        DatasetDmsServiceMapImpl serviceMap = new DatasetDmsServiceMapImpl(headers, configBag, cache, queryHelperFactory, logger);

        when(cache.get(expectedKey)).thenReturn(registrationsMock);

        Map<String, DmsServiceProperties> serviceProps = new HashMap<>();
        when(registrationsMock.getDynamoDmsRegistrations()).thenReturn(serviceProps);

        Map<String, DmsServiceProperties> resultProps = serviceMap.getResourceTypeToDmsServiceMap();

        assertEquals(serviceProps, resultProps);

        verify(queryHelper, never()).scanTable(DynamoDmsRegistration.class);
        verify(cache, never()).put(anyString(), any());
        verify(logger, never()).info(anyString());
        verify(logger, never()).error(anyString(), any(Exception.class));
    }
}
