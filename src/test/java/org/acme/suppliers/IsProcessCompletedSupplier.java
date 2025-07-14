package org.acme.suppliers;

import java.util.Optional;
import java.util.function.Supplier;
import org.kie.kogito.Model;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;

public class IsProcessCompletedSupplier implements Supplier<Boolean> {
    final Process<? extends Model> process;
    final String id;

    public IsProcessCompletedSupplier(Process<? extends Model> process, String id) {
        this.process = process;
        this.id = id;
    }

    @Override
    public Boolean get() {
        Optional<? extends ProcessInstance<? extends Model>> processById =
                process.instances().findById(id);
        return processById.isEmpty() || processById.get().status() == ProcessInstance.STATE_COMPLETED;
    }
}
