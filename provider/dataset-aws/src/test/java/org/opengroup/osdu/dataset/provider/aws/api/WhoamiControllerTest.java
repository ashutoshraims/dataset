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

package org.opengroup.osdu.dataset.provider.aws.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
@RunWith(MockitoJUnitRunner.class)
class WhoamiControllerTest {

    private WhoamiController controller;

    @Mock
    Authentication authentication;

    @Mock
    SecurityContext securityContext;

    public WhoamiControllerTest() {
    }

    @Before
    public void setup() {
        controller = new WhoamiController();
    }

    @Test
    void when_WhoamiController_whoami_returnsValidResponse() throws Exception {
        final String username = "username";
        final String details = "some details";
        try (MockedStatic<SecurityContextHolder> mockedSecurityCtxHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityCtxHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getName()).thenReturn(username);
            when(authentication.getPrincipal()).thenReturn(details);
            when(authentication.getAuthorities()).thenReturn(new ArrayList<>());

            String pattern = String.format("^\\s*user:\\s*%s<BR>roles:\\s*%s<BR>details:\\s*%s<BR>\\s*$", username, ".*", details);

            String result = controller.whoami();
            assertThat(result, matchesPattern(pattern));
        }
    }

}
