package org;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.constants.KogitoConstants;
import org.writers.JsonObjectWriter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = true)
public class CloudEventHelmEngineActivationResultMessage extends CloudEventBase
        implements Serializable {
    @JsonProperty(KogitoConstants.DATA)
    private HelmEngineActivationResult helmEngineActivationResult;

    public String toStringAsJson() {
        return JsonObjectWriter.writeValueAsJsonString(this);
    }
}
