package org.opengroup.osdu.dataset.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.opengroup.osdu.dataset.TenantUtils;

public class GcHeaderUtils {

    public static Map<String, String> getHeaders(String tenantName, String token) {
        Map<String, String> headers = new HashMap<>();
        if(tenantName == null || tenantName.isEmpty()) {
            tenantName = TenantUtils.getTenantName();
        }
        headers.put("Data-Partition-Id", tenantName);
        headers.put("Authorization", token);

        final String correlationId = UUID.randomUUID().toString();
        System.out.printf("Using correlation-id for the request: %s \n", correlationId);
        headers.put("correlation-id", correlationId);
        return headers;
    }
}
