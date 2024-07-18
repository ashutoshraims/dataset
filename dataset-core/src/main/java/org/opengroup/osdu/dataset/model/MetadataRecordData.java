package org.opengroup.osdu.dataset.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MetadataRecordData {
    @JsonProperty("metadataid")
    private String metadataId;

    @JsonProperty("datetime")
    private String datetime;
}
