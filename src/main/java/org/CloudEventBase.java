package org;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.constants.KogitoConstants;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CloudEventBase implements Serializable {
    @JsonProperty(KogitoConstants.SPEC_VERSION)
    private String specVersion;

    private UUID id;
    private String source;
    private String type;

    @JsonProperty(KogitoConstants.KOGITO_PROCESS_REFERENCE_ID)
    private UUID processReferenceId;

    @JsonProperty(KogitoConstants.KOGITO_PROCESS_INSTANCE_ID)
    private UUID processInstanceId;

    @JsonProperty(KogitoConstants.KOGITO_PROCESS_ID)
    private String processId;

    @JsonProperty(KogitoConstants.KOGITO_PARENT_PROC_IID)
    private UUID parentProcessId;
}
