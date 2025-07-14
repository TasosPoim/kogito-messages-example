package org.acme.helpers;

import static org.constants.KogitoConstants.HELM_ENGINE_ACTIVATION;
import static org.constants.KogitoConstants.HELM_ENGINE_ACTIVATION_RESULT;
import static org.constants.KogitoConstants.TMF_SERVICE_ACTIVATION;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import java.util.HashMap;
import java.util.Map;

public class InMemoryKafkaBrokerResource implements QuarkusTestResourceLifecycleManager {
    @Override
    public Map<String, String> start() {
        Map<String, String> env = new HashMap<>();

        env.putAll(InMemoryConnector.switchOutgoingChannelsToInMemory(HELM_ENGINE_ACTIVATION));

        env.putAll(
                InMemoryConnector.switchIncomingChannelsToInMemory(
                        HELM_ENGINE_ACTIVATION_RESULT, TMF_SERVICE_ACTIVATION));

        return env;
    }

    @Override
    public void stop() {
        InMemoryConnector.clear();
    }
}
