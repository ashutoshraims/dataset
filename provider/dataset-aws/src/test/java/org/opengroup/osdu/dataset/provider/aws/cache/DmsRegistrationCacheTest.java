// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package org.opengroup.osdu.dataset.provider.aws.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.aws.ssm.K8sParameterNotFoundException;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.dataset.provider.aws.config.ProviderConfigurationBag;
import org.opengroup.osdu.dataset.provider.aws.model.DmsRegistrations;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class DmsRegistrationCacheTest extends CacheTest<String, DmsRegistrations>{
    private final Map<String, String> REDIS_CREDENTIALS = new HashMap<>();

    private final ProviderConfigurationBag providerConfig = new ProviderConfigurationBag();

    private final String REDIS_SEARCH_KEY = "STATIC_REDIS_SEARCH_KEY";

    private final DmsRegistrations REGISTRATIONS = new DmsRegistrations();

    private final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

    private final ArgumentCaptor<DmsRegistrations> registrationCaptor = ArgumentCaptor.forClass(DmsRegistrations.class);

    @Before
    public void setup() {
        providerConfig.redisSearchKey = REDIS_SEARCH_KEY;
    }

    @Override
    public ICache<String, DmsRegistrations> createDefaultCache() throws K8sParameterNotFoundException, JsonProcessingException  {
        return new DmsRegistrationCache(providerConfig);
    }

    @Override
    public String getKey() {
        return "some-key";
    }

    @Override
    public DmsRegistrations getValue() {
        return REGISTRATIONS;
    }

    @Test
    public void should_return_dummyCache_when_localSetWithDisableCache() throws Exception {
        super.testCreateWithDummyCache("cache");
    }

    @Test
    public void should_return_basicCache_when_localSetWithoutDisableCache() throws K8sParameterNotFoundException, JsonProcessingException {
        super.testCreateWithVmCache("cache");
    }

    private void run_redisCache_withNonLocal(Map<String, String> redisCreds, String password) throws K8sParameterNotFoundException, JsonProcessingException {
        super.testCreateWithoutLocalMode("cache", redisCreds, password, String.class, DmsRegistrations.class);
    }

    @Test
    public void should_return_redisCache_when_runningInNonLocalWithCredentials() throws K8sParameterNotFoundException, JsonProcessingException {
        String REDIS_CREDENTIALS_TOKEN = "redisCredentialsToken";
        REDIS_CREDENTIALS.put("token", REDIS_CREDENTIALS_TOKEN);
        run_redisCache_withNonLocal(REDIS_CREDENTIALS, REDIS_CREDENTIALS_TOKEN);
    }

    @Test
    public void should_return_redisCache_when_runningInNonLocalWithoutCredentials() throws K8sParameterNotFoundException, JsonProcessingException {
        run_redisCache_withNonLocal(null, REDIS_SEARCH_KEY);
    }

    @Test
    public void should_return_ExpectedHash_when_getGroupCacheKeyCalled() {
        testGetGroupCacheKey("dms-registration", DmsRegistrationCache::getCacheKey);
    }

    @Test
    public void should_return_when_putCalled() throws K8sParameterNotFoundException, JsonProcessingException {
        super.testPutCache();
    }

    @Test
    public void should_returnDmsRegistration_when_getCalled() throws K8sParameterNotFoundException, JsonProcessingException {
        super.testGetCache();
    }

    @Test
    public void should_return_when_deleteCalled() throws K8sParameterNotFoundException, JsonProcessingException {
        super.testDeleteCache();
    }

    @Test
    public void should_return_when_clearAllCalled() throws K8sParameterNotFoundException, JsonProcessingException {
        super.testClearAllCache();
    }
}
