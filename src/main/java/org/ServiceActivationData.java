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
public class ServiceActivationData implements Serializable {
    private UUID serviceOrderId;
    private UUID serviceOrderItemId;
    private UUID serviceId;
    private boolean activateService;
}
