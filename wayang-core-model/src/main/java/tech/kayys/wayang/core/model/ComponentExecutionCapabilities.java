package tech.kayys.wayang.core.model;

import java.util.Objects;
import java.util.Set;

/** Static execution claims made by a component provider; live health belongs to runtime inventory. */
public record ComponentExecutionCapabilities(
        Set<ExecutionMode> supportedModes,
        StateModel stateModel,
        boolean payloadPortable,
        boolean cancellationSupported,
        boolean idempotent) {
    public ComponentExecutionCapabilities {
        supportedModes = Set.copyOf(Objects.requireNonNull(supportedModes, "supportedModes"));
        if (supportedModes.isEmpty()) throw new IllegalArgumentException("At least one execution mode is required");
        Objects.requireNonNull(stateModel, "stateModel");
        if ((supportedModes.contains(ExecutionMode.REMOTE) || supportedModes.contains(ExecutionMode.CLOUD_NATIVE))
                && !payloadPortable) {
            throw new IllegalArgumentException("Remote/cloud-native modes require portable payloads");
        }
    }
}
