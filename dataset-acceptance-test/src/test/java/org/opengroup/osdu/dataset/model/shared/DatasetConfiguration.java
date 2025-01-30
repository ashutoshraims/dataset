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

package org.opengroup.osdu.dataset.model.shared;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.hamcrest.core.Is;
import org.opengroup.osdu.dataset.HeaderUtils;
import org.opengroup.osdu.dataset.TenantUtils;
import org.opengroup.osdu.dataset.TestUtils;
import org.opengroup.osdu.dataset.model.configuration.TestConfig;
import org.opengroup.osdu.dataset.util.FileUtils;

import java.io.IOException;

import static org.hamcrest.core.AnyOf.anyOf;
import static org.junit.Assert.assertThat;


@Slf4j
public class DatasetConfiguration {

    private static final String INPUT_DATASET_FILE_SCHEMA_JSON = "input/datasetFileSchema.json";
    private static final String INPUT_DATASET_FILE_COLLECTION_SCHEMA_JSON = "input/datasetFileCollectionSchema.json";
    private static final String INPUT_LEGALTAG_JSON = "input/legaltag.json";

    public static void datasetSetup(String token) throws Exception {
        String datasetFileSchema = getDatasetSchema(INPUT_DATASET_FILE_SCHEMA_JSON);

        CloseableHttpResponse createFileSchemaResponse = TestUtils.send(TestConfig.getSchemaServiceHost(), "/schema", "POST",
            HeaderUtils.getHeaders(TenantUtils.getTenantName(), token),
            datasetFileSchema);

        assertThat(createFileSchemaResponse.getCode(), anyOf(Is.is(201), Is.is(400)));
        log.info("create dataset file schema response status:" + createFileSchemaResponse.getCode());

        String datasetCollectionSchema = getDatasetSchema(INPUT_DATASET_FILE_COLLECTION_SCHEMA_JSON);

        CloseableHttpResponse createCollectionSchemaResponse = TestUtils
            .send(TestConfig.getSchemaServiceHost(), "/schema", "POST",
                HeaderUtils.getHeaders(TenantUtils.getTenantName(), token),
                datasetCollectionSchema);

        assertThat(createCollectionSchemaResponse.getCode(), anyOf(Is.is(201), Is.is(400)));
        log.info("create dataset collection schema response status:" + createCollectionSchemaResponse.getCode());

        String legalTag = getLegalTag(INPUT_LEGALTAG_JSON);

        CloseableHttpResponse createLegalTagResponse = TestUtils.send(TestUtils.LEGAL_BASE_URL, "legaltags", "POST",
            HeaderUtils.getHeaders(TenantUtils.getTenantName(), token),
            legalTag);

        assertThat(createLegalTagResponse.getCode(), anyOf(Is.is(201), Is.is(409)));
        log.info("create legal tag response status: " + createLegalTagResponse.getCode());
    }

    private static String getDatasetSchema(String schemaFile) throws IOException {
        String datasetFileSchema = FileUtils.readFileFromResources(schemaFile);
        StringSubstitutor stringSubstitutor = new StringSubstitutor(
            ImmutableMap.of(
                "tenant", TenantUtils.getTenantName(),
                "domain", TestUtils.getDomain(),
                "kind-subtype", TestConfig.getDatasetKindSubType()
            ));
        return stringSubstitutor.replace(datasetFileSchema);
    }

    private static String getLegalTag(String tagFile) throws IOException {
        String legalTag = FileUtils.readFileFromResources(tagFile);
        StringSubstitutor stringSubstitutor = new StringSubstitutor(
            ImmutableMap.of(
                "legal-tag", TestConfig.getLegalTag()
            ));
        return stringSubstitutor.replace(legalTag);
    }

}
