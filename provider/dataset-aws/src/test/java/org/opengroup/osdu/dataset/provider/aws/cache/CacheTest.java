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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.opengroup.osdu.core.aws.cache.DummyCache;
import org.opengroup.osdu.core.aws.ssm.K8sLocalParameterProvider;
import org.opengroup.osdu.core.aws.ssm.K8sParameterNotFoundException;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.cache.RedisCache;
import org.opengroup.osdu.core.common.cache.VmCache;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.util.Crc32c;
import org.opengroup.osdu.dataset.provider.aws.model.DmsRegistrations;
import org.springframework.test.util.ReflectionTestUtils;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class CacheTest<K, V> {

    public abstract ICache<K, V> createDefaultCache() throws K8sParameterNotFoundException, JsonProcessingException;

    public void testCreateWithDummyCache(String cacheInternalVar) throws Exception {
        try (MockedConstruction<K8sLocalParameterProvider> k8sParameterProvider = Mockito.mockConstruction(K8sLocalParameterProvider.class,
                                                                                                           (mock, context) -> {
                                                                                                               when(mock.getLocalMode()).thenReturn(true);
                                                                                                           })) {

            EnvironmentVariables environmentVariables = new EnvironmentVariables();
            environmentVariables.setup();
            environmentVariables.set("DISABLE_CACHE", "true");

            ICache<K, V> cache = createDefaultCache();
            environmentVariables.teardown();

            assertTrue(ReflectionTestUtils.getField(cache, cacheInternalVar) instanceof DummyCache);
        }
    }

    public void testCreateWithVmCache(String cacheInternalVar) throws K8sParameterNotFoundException, JsonProcessingException {
        try (MockedConstruction<K8sLocalParameterProvider> k8sParameterProvider = Mockito.mockConstruction(K8sLocalParameterProvider.class,
                                                                                                           (mock, context) -> {
                                                                                                               when(mock.getLocalMode()).thenReturn(true);
                                                                                                           })) {
            ICache<K, V> cache = createDefaultCache();
            assertTrue(ReflectionTestUtils.getField(cache, cacheInternalVar) instanceof VmCache);
        }
    }

    public void testCreateWithoutLocalMode(String cacheInternalVar, Map<String, String> credentialsMap, String token, Class<K> keyClass, Class<V> valueClass) throws K8sParameterNotFoundException, JsonProcessingException {
        final String REDIS_HOSTNAME = "redis.hostname.com";
        final int REDIS_PORT = 123435;

        try (MockedConstruction<K8sLocalParameterProvider> k8sParameterProvider = Mockito.mockConstruction(K8sLocalParameterProvider.class,
                                                                                                           (mock, context) -> {
                                                                                                               when(mock.getLocalMode()).thenReturn(false);
                                                                                                               when(mock.getParameterAsStringOrDefault(eq("CACHE_CLUSTER_ENDPOINT"), any())).thenReturn(REDIS_HOSTNAME);
                                                                                                               when(mock.getParameterAsStringOrDefault(eq("CACHE_CLUSTER_PORT"), any())).thenReturn(Integer.toString(REDIS_PORT));
                                                                                                               when(mock.getCredentialsAsMap("CACHE_CLUSTER_KEY")).thenReturn(credentialsMap);
                                                                                                           })) {
            List<Object> redisArgs = new ArrayList<>();
            try (MockedConstruction<RedisCache> redisCache = Mockito.mockConstruction(RedisCache.class,
                                                                                      (mock, context) -> {
                                                                                          redisArgs.addAll(context.arguments());
                                                                                      })){
                ICache<K, V> cache = createDefaultCache();
                assertTrue(ReflectionTestUtils.getField(cache, cacheInternalVar) instanceof RedisCache);

                assertEquals(6, redisArgs.size());
                assertEquals(REDIS_HOSTNAME, redisArgs.get(0));
                assertEquals(REDIS_PORT, redisArgs.get(1));
                assertEquals(token, redisArgs.get(2));
                assertEquals(keyClass, redisArgs.get(4));
                assertEquals(valueClass, redisArgs.get(5));
            }
        }
    }

    public void testHashMatches(Supplier<String> supplier, String expectedUnHashed) {
        String expectedHash = Crc32c.hashToBase64EncodedString(expectedUnHashed);

        assertEquals(expectedHash, supplier.get());
    }

    public void testGetGroupCacheKey(String prefix, Function<DpsHeaders, String> getCacheKeyFunction) {
        DpsHeaders headers = new DpsHeaders();
        final String AUTHORIZATION = "testAuthorizationToken";

        String DATA_PARTITION_ID = "test-data-partition-id";
        headers.put("data-partition-id", DATA_PARTITION_ID);
        headers.put("authorization", AUTHORIZATION);

        String expectedUnHash = String.format("%s:%s:%s", prefix, DATA_PARTITION_ID, AUTHORIZATION);

        testHashMatches(() -> getCacheKeyFunction.apply(headers), expectedUnHash);
    }

    public abstract K getKey();

    public abstract V getValue();

    public void testPutCache() throws K8sParameterNotFoundException, JsonProcessingException {
        try (MockedConstruction<K8sLocalParameterProvider> k8sParameterProvider = Mockito.mockConstruction(K8sLocalParameterProvider.class,
                                                                                                           (mock, context) -> {
                                                                                                               when(mock.getLocalMode()).thenReturn(true);
                                                                                                           })) {
            try (MockedConstruction<VmCache> vmCache = Mockito.mockConstruction(VmCache.class)) {
                ICache<K,V> cache = createDefaultCache();

                assertEquals(1, vmCache.constructed().size());

                VmCache<K, V> mockedVmCache = vmCache.constructed().get(0);

                K key = getKey();
                V value = getValue();
                doNothing().when(mockedVmCache).put(key, value);

                cache.put(key, value);

                verify(mockedVmCache, times(1)).put(key, value);
            }
        }
    }

    public void testGetCache() throws K8sParameterNotFoundException, JsonProcessingException {
        try (MockedConstruction<K8sLocalParameterProvider> k8sParameterProvider = Mockito.mockConstruction(K8sLocalParameterProvider.class,
                                                                                                           (mock, context) -> {
                                                                                                               when(mock.getLocalMode()).thenReturn(true);
                                                                                                           })) {
            try (MockedConstruction<VmCache> vmCache = Mockito.mockConstruction(VmCache.class)) {
                ICache<K,V> cache = createDefaultCache();

                assertEquals(1, vmCache.constructed().size());

                VmCache<K, V> mockedVmCache = vmCache.constructed().get(0);

                K key = getKey();
                V value = getValue();
                doReturn(value).when(mockedVmCache).get(key);

                V result = cache.get(key);

                verify(mockedVmCache, times(1)).get(key);
                assertEquals(value, result);
            }
        }
    }

    public void testDeleteCache() throws K8sParameterNotFoundException, JsonProcessingException {
        try (MockedConstruction<K8sLocalParameterProvider> k8sParameterProvider = Mockito.mockConstruction(K8sLocalParameterProvider.class,
                                                                                                           (mock, context) -> {
                                                                                                               when(mock.getLocalMode()).thenReturn(true);
                                                                                                           })) {
            try (MockedConstruction<VmCache> vmCache = Mockito.mockConstruction(VmCache.class)) {
                ICache<K,V> cache = createDefaultCache();

                assertEquals(1, vmCache.constructed().size());

                VmCache<K, V> mockedVmCache = vmCache.constructed().get(0);

                K key = getKey();
                doNothing().when(mockedVmCache).delete(key);

                cache.delete(key);

                verify(mockedVmCache, times(1)).delete(key);
            }
        }
    }

    public void testClearAllCache() throws K8sParameterNotFoundException, JsonProcessingException {
        try (MockedConstruction<K8sLocalParameterProvider> k8sParameterProvider = Mockito.mockConstruction(K8sLocalParameterProvider.class,
                                                                                                           (mock, context) -> {
                                                                                                               when(mock.getLocalMode()).thenReturn(true);
                                                                                                           })) {
            try (MockedConstruction<VmCache> vmCache = Mockito.mockConstruction(VmCache.class)) {
                ICache<K,V> cache = createDefaultCache();

                assertEquals(1, vmCache.constructed().size());

                VmCache<K, V> mockedVmCache = vmCache.constructed().get(0);

                doNothing().when(mockedVmCache).clearAll();

                cache.clearAll();

                verify(mockedVmCache, times(1)).clearAll();
            }
        }
    }
}
