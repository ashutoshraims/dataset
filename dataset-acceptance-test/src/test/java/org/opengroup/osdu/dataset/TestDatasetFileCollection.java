package org.opengroup.osdu.dataset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.*;
import org.opengroup.osdu.core.common.dms.model.StorageInstructionsResponse;
import org.opengroup.osdu.dataset.model.configuration.MapperConfig;
import org.opengroup.osdu.dataset.util.LegalTagUtils;
import org.opengroup.osdu.dataset.util.TokenTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * End-to-end functionality cannot be generically tested due to varying storage implementations across CSPs.
 * This test only verifies that the signing options are not null.
 */
public final class TestDatasetFileCollection extends TestBase {

    private static TestUtils fileCollectionTokenUtils = new TokenTestUtils();
    private static String LEGAL_TAG = LegalTagUtils.createRandomName();
    private static final String SIGNING_OPTIONS = "signingOptions";
    private ObjectMapper jsonMapper = MapperConfig.getObjectMapper();

    @BeforeClass
    public static void classSetup() throws Exception {
        classSetup(fileCollectionTokenUtils.getToken());
    }

    @AfterClass
    public static void classTearDown() throws Exception {
        classTearDown(fileCollectionTokenUtils.getToken());
    }

    @Test
    public void validate_storage_instructions() throws Exception {
        String kindSubType = "dataset--FileCollection.Generic";

        //Get Storage Instructions for File
        StorageInstructionsResponse storageInstResponse = storageInstructions(kindSubType);
        validateStorageInstructions(storageInstResponse);
    }

    public void validateStorageInstructions(StorageInstructionsResponse storageInstructions) {
        assertNotNull(storageInstructions);
        assertEquals(TestUtils.PROVIDER_KEY, storageInstructions.getProviderKey());
        assertNotNull(storageInstructions.getStorageLocation());
        assertNotNull(storageInstructions.getStorageLocation().get(SIGNING_OPTIONS));
    }

    public static void classSetup(String token) throws Exception {
        LegalTagUtils.create(
                HeaderUtils.getHeaders(TenantUtils.getTenantName(), token),
                LEGAL_TAG);
    }

    public static void classTearDown(String token) throws Exception {
        LegalTagUtils.delete(HeaderUtils.getHeaders(TenantUtils.getTenantName(), token), LEGAL_TAG);
    }

    private StorageInstructionsResponse storageInstructions(String kindSubType) throws Exception {
        StorageInstructionsResponse getStorageInstResponse = getTestStorageInstructions(kindSubType, 200);
        validateStorageInstructions(getStorageInstResponse);
        return getStorageInstResponse;
    }

    private StorageInstructionsResponse getTestStorageInstructions(String kindSubType, int expectedStatusCode) throws Exception {
        CloseableHttpResponse getStorageInstClientResp = TestUtils.send("storageInstructions", "POST",
                HeaderUtils.getHeaders(TenantUtils.getTenantName(), testUtils.getToken()),
                "", String.format("?kindSubType=%s", kindSubType));

        int actualStatusCode = getStorageInstClientResp.getCode();
        assertEquals(expectedStatusCode, actualStatusCode);

        if (actualStatusCode != 200) {
            return null;
        }
        String getStorageRespStr = EntityUtils.toString(getStorageInstClientResp.getEntity());
        return jsonMapper.readValue(getStorageRespStr, StorageInstructionsResponse.class);
    }

    @Before
    @Override
    public void setup() {
        testUtils = new TokenTestUtils();
    }

    @After
    @Override
    public void tearDown() {
        testUtils = null;
    }
}
