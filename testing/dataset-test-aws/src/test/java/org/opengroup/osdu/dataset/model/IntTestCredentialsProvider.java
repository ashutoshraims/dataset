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

package org.opengroup.osdu.dataset.model;

import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.AWSSessionCredentialsProvider;

public class IntTestCredentialsProvider implements AWSSessionCredentialsProvider {

    private IntTestCredentials credentials;

    public IntTestCredentialsProvider(IntTestCredentials credentials) {
        this.credentials = credentials;

    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    public AWSSessionCredentials getCredentials() {
        return credentials;
    }
    
}
