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

package org.opengroup.osdu.dataset.anthos.configuration;

public class AnthosConfig {

    public static String getSchemaServiceHost() {
        return getEnvironmentVariableOrDefaultValue("SCHEMA_API", "");
    }

    public static String getDatasetKindSubType() {
        return getEnvironmentVariableOrDefaultValue("KIND_SUBTYPE", "");
    }

    public static String getLegalTag() {
        return getEnvironmentVariableOrDefaultValue("LEGAL_TAG", "");
    }

    public static String getGcpStoragePersistentArea() {
        return getEnvironmentVariableOrDefaultValue("BAREMETAL_STORAGE_PERSISTENT_AREA", "");
    }

    private static String getEnvironmentVariableOrDefaultValue(String key, String defaultValue) {
        String environmentVariable = getEnvironmentVariable(key);
        if (environmentVariable == null) {
            environmentVariable = defaultValue;
        }
        return environmentVariable;
    }

    private static String getEnvironmentVariable(String propertyKey) {
        return System.getProperty(propertyKey, System.getenv(propertyKey));
    }

}
