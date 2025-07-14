package org.acme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.constants.KogitoConstants.HELM_ENGINE_ACTIVATION;
import static org.constants.KogitoConstants.HELM_ENGINE_ACTIVATION_RESULT;
import static org.constants.KogitoConstants.HELM_ENGINE_ACTIVATION_SUBPROCESS;
import static org.constants.KogitoConstants.TMF_SERVICE_ACTIVATION;
import static org.kie.kogito.test.utils.ProcessInstancesTestUtils.abort;
import static org.kie.kogito.test.utils.ProcessInstancesTestUtils.assertEmpty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.JsonObject;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import io.smallrye.reactive.messaging.memory.InMemorySource;
import jakarta.enterprise.inject.Any;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Named;
import org.CloudEventHelmEngineActivationResultMessage;
import org.CloudEventServiceActivationState;
import org.ServiceActivationData;
import org.acme.helpers.InMemoryKafkaBrokerResource;
import org.acme.helpers.TestHelpers;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Model;
import org.kie.kogito.process.Process;

@QuarkusTest
@QuarkusTestResource(value = InMemoryKafkaBrokerResource.class)
class HelmEngineProcessesTest {

    public static final Duration DURATION = Duration.ofSeconds(10);
    public static final String HELM_ENGINE_ACTIVATION_MESSAGES = "helmEngineActivationMessages";
    public static final String SONATA_SERVICE_ACTIVATION_MESSAGE_JSON =
            "sonataServiceActivationMessage.json";

    @Inject
    @Named(HELM_ENGINE_ACTIVATION_SUBPROCESS)
    Process<? extends Model> helmEngineServiceActivationSubprocess;

    @Inject @Any InMemoryConnector connector;

    InMemorySource<String> incomingChannelFromHelmEngineActivation;
    InMemorySink<String> outgoingChannelToHelmEngineActivation;

    InMemorySource<String> incomingChannelFromTmfServiceActivation;

    ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .registerModule(new JavaTimeModule());

    @BeforeEach
    void init() {
        // initiate model
        incomingChannelFromTmfServiceActivation = connector.source(TMF_SERVICE_ACTIVATION);

        incomingChannelFromHelmEngineActivation = connector.source(HELM_ENGINE_ACTIVATION_RESULT);
        outgoingChannelToHelmEngineActivation = connector.sink(HELM_ENGINE_ACTIVATION);

        // clean up outgoing KAFKA channels
        outgoingChannelToHelmEngineActivation.clear();

        // clean up process instances
        abort(helmEngineServiceActivationSubprocess.instances());
    }

    @Test
    void helmEngineActivateServiceProcessTest()
            throws FileNotFoundException, JsonProcessingException {
        JsonObject tmfServiceActivationIncomingMessage =
                TestHelpers.getJsonObjectFromFileReader(
                        HELM_ENGINE_ACTIVATION_MESSAGES, SONATA_SERVICE_ACTIVATION_MESSAGE_JSON);
        CloudEventServiceActivationState cloudEventServiceActivationState =
                objectMapper.readValue(
                        tmfServiceActivationIncomingMessage.toString(), CloudEventServiceActivationState.class);

        incomingChannelFromTmfServiceActivation.send(cloudEventServiceActivationState.toStringAsJson());

        TestHelpers.pollProcessInstances(helmEngineServiceActivationSubprocess, 1)
                .await()
                .atMost(DURATION);

        // POLL IN ORDER TO "FORCE" THE TEST TO WAIT UNTIL THE PROCESS REACHES THE INTERMEDIATE STEP
        TestHelpers.pollKafkaChannel(outgoingChannelToHelmEngineActivation, 1).await().atMost(DURATION);

        // retrieve the incoming messages
        List<? extends Message<String>> received = outgoingChannelToHelmEngineActivation.received();

        assertThat(received).hasSize(1);

        // OBTAIN THE MESSAGE THAT WAS SENT TO HELM ENGINE
        Message<String> stringMessageHelmEngineActivation = received.get(0);
        CloudEventServiceActivationState eventServiceActivationStateReceived =
                objectMapper.readValue(
                        stringMessageHelmEngineActivation.getPayload(), CloudEventServiceActivationState.class);
        ServiceActivationData serviceActivationDataReceived =
                eventServiceActivationStateReceived.getServiceActivationData();
        assertThat(serviceActivationDataReceived).isNotNull();
        assertThat(serviceActivationDataReceived.getServiceId()).isNotNull();

        UUID parentProcessId = eventServiceActivationStateReceived.getParentProcessId();
        String kogitoParentProcessInstanceId =
                parentProcessId != null
                        ? parentProcessId.toString()
                        : eventServiceActivationStateReceived.getProcessInstanceId().toString();

        UUID kogitoProcessInstanceId = eventServiceActivationStateReceived.getProcessInstanceId();

        // trigger the intermediate step ( mock the Helm Engine Response)

        // prepare the incoming message
        JsonObject incomingHelmEngineResponseAsJson =
                TestHelpers.getJsonObjectFromFileReader(
                        HELM_ENGINE_ACTIVATION_MESSAGES, "serviceActivationResultMessage.json");

        CloudEventHelmEngineActivationResultMessage cloudEventHelmEngineActivationResultMessage =
                objectMapper.readValue(
                        incomingHelmEngineResponseAsJson.toString(),
                        CloudEventHelmEngineActivationResultMessage.class);
        cloudEventHelmEngineActivationResultMessage.setProcessReferenceId(kogitoProcessInstanceId);

        // send the incoming message to sonata to fire the intermediate trigger and complete the process
        incomingChannelFromHelmEngineActivation.send(
                cloudEventHelmEngineActivationResultMessage.toStringAsJson());

        // poll until process is completed
        TestHelpers.pollProcessCompletionStatus(
                        helmEngineServiceActivationSubprocess, kogitoParentProcessInstanceId)
                .await()
                .atMost(DURATION);

        assertEmpty(helmEngineServiceActivationSubprocess.instances());
    }
}
