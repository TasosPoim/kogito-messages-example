package org.acme.helpers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.providers.connectors.InMemorySink;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.Duration;
import org.acme.suppliers.HasReceivedMessagesSupplier;
import org.acme.suppliers.IsProcessCompletedSupplier;
import org.acme.suppliers.NumberOfProcessesSupplier;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.kie.kogito.Model;
import org.kie.kogito.process.Process;

public final class TestHelpers {

    public static final String SRC = "src";
    public static final String TEST = "test";
    public static final String RESOURCES = "resources";
    public static final String TEST_MESSAGES = "testMessages";

    public static Uni<Boolean> pollKafkaChannel(
            InMemorySink<?> inMemorySink, int wantedNumberOfMessages) {
        return Multi.createBy()
                .repeating()
                .supplier(new HasReceivedMessagesSupplier(inMemorySink, wantedNumberOfMessages))
                .withDelay(Duration.ofMillis(100))
                .whilst(s -> (s == null || !s))
                .collect()
                .last();
    }

    public static Uni<Boolean> pollProcessCompletionStatus(
            Process<? extends Model> process, String processInstanceId) {
        return Multi.createBy()
                .repeating()
                .supplier(new IsProcessCompletedSupplier(process, processInstanceId))
                .withDelay(Duration.ofMillis(100))
                .whilst(s -> (s == null || !s))
                .collect()
                .last();
    }

    public static Uni<Boolean> pollProcessInstances(
            Process<? extends Model> process, int numberOfInstances) {
        return Multi.createBy()
                .repeating()
                .supplier(new NumberOfProcessesSupplier(process, numberOfInstances))
                .withDelay(Duration.ofMillis(100))
                .whilst(s -> (s == null || !s))
                .collect()
                .last();
    }

    @NotNull
    public static FileReader getFileReader(String fileSubdirectory, String fileName)
            throws FileNotFoundException {
        return new FileReader(FileUtils.getFile(SRC, TEST, RESOURCES, fileSubdirectory, fileName));
    }

    public static JsonObject getJsonObjectFromFileReader(String fileSubdirectory, String fileName)
            throws FileNotFoundException {
        return JsonParser.parseReader(getFileReader(fileSubdirectory, fileName)).getAsJsonObject();
    }
}
