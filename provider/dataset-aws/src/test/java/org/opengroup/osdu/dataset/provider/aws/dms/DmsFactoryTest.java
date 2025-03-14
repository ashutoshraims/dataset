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

package org.opengroup.osdu.dataset.provider.aws.dms;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.dataset.dms.DmsServiceProperties;



class DmsFactoryTest {

    private DpsHeaders dpsHeaders;

    private DmsServiceProperties dmsServiceProperties;

    private DmsFactory dmsFactory;

    @BeforeEach
    public void setUp() {
        dpsHeaders = new DpsHeaders();
        dmsServiceProperties = new DmsServiceProperties("https://bogusurl");
        dmsFactory = new DmsFactory();
    }

    @Test
    void testCreateDmsService() {
        assertDoesNotThrow(() -> {dmsFactory.create(dpsHeaders, dmsServiceProperties);}, "DmsFactory.create(...) should not throw an exception!");
    }

}
