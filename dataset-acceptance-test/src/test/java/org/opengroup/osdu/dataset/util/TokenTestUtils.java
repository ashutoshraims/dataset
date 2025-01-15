/*
 * Copyright 2020-2022 Google LLC
 * Copyright 2020-2022 EPAM Systems, Inc
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

package org.opengroup.osdu.dataset.util;

import com.sun.jersey.api.client.Client;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.dataset.TestUtils;

import java.util.Optional;

public class TokenTestUtils extends TestUtils {

    private OpenIDTokenProvider tokenProvider = null;

    public TokenTestUtils() {
        token = System.getProperty("PRIVILEGED_USER_TOKEN", System.getenv("PRIVILEGED_USER_TOKEN"));
        //noDataAccessToken = System.getProperty("NO_ACCESS_USER_TOKEN", System.getenv("NO_ACCESS_USER_TOKEN"));
        domain = Optional.ofNullable(System.getProperty("GROUP_ID", System.getenv("GROUP_ID")))
                .orElse("group");

        if(StringUtils.isAnyEmpty(token)) {
            tokenProvider = new OpenIDTokenProvider();
        }
    }

    public String getToken() {
        if (StringUtils.isEmpty(token)) {
            token = tokenProvider.getToken();
        }
        return "Bearer " + token;
    }

    public static Client getClient() {
        return TestUtils.getClient();
    }
}