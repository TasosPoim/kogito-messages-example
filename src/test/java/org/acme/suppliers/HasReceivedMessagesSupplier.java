package org.acme.suppliers;

import io.smallrye.reactive.messaging.providers.connectors.InMemorySink;
import java.util.function.Supplier;

public class HasReceivedMessagesSupplier implements Supplier<Boolean> {
    final InMemorySink<?> inMemorySink;
    int wantedNumberOfMessagesReceived;

    public HasReceivedMessagesSupplier(
            InMemorySink<?> inMemorySink, int wantedNumberOfMessagesReceived) {
        this.inMemorySink = inMemorySink;
        this.wantedNumberOfMessagesReceived = wantedNumberOfMessagesReceived;
    }

    @Override
    public Boolean get() {
        return inMemorySink.received().size() == wantedNumberOfMessagesReceived;
    }
}
