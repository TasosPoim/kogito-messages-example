package org;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HelmEngineActivationResult implements Serializable {
    private UUID serviceId;
    private UUID locationId;
    private UUID serviceOrderId;
    private String serviceNamespace;
    private String namespaceResourceQuota;
    private String helmEngineOrderMessage;
}
