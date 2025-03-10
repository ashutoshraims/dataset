/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.dataset.dms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;

@RunWith(MockitoJUnitRunner.class)
public class DmsFactoryTest {
    @Mock
    private DpsHeaders headers;

    @Mock
    private DmsServiceProperties dmsServiceProperties;

    @InjectMocks
    private DmsFactory dmsFactory;

    @Test
    public void create_success_return_DmsRestService_Object() {
        IDmsProvider dmsRestService = dmsFactory.create(headers, dmsServiceProperties);
        assertNotNull(dmsRestService);
        assertTrue(dmsRestService instanceof DmsService);
    }

    @Test
    public void create_failure_NullPointerException() {
        try {
            dmsFactory.create(null, dmsServiceProperties);
        }
        catch (Exception exception) {
            assertNotNull(exception);
            assertTrue(exception instanceof NullPointerException);
            assertEquals("headers cannot be null", exception.getMessage());
        }
    }
}
