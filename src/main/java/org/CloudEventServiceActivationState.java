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
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CloudEventServiceActivationState extends CloudEventBase implements Serializable {
    @JsonProperty(KogitoConstants.DATA)
    private ServiceActivationData serviceActivationData;

    public String toStringAsJson() {
        return JsonObjectWriter.writeValueAsJsonString(this);
    }
}
