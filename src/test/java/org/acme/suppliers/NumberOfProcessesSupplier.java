package org.acme.suppliers;

import java.util.function.Supplier;
import org.kie.kogito.Model;
import org.kie.kogito.process.Process;

public class NumberOfProcessesSupplier implements Supplier<Boolean> {
    final Process<? extends Model> process;
    final int numberOfProcesses;

    public NumberOfProcessesSupplier(Process<? extends Model> process, int numberOfProcesses) {
        this.process = process;
        this.numberOfProcesses = numberOfProcesses;
    }

    @Override
    public Boolean get() {
        return process.instances().stream().count() == numberOfProcesses;
    }
}
