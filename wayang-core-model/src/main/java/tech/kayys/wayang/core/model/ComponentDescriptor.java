package tech.kayys.wayang.core.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable metadata for a versioned component capability; contains no executable or host services. */
public record ComponentDescriptor(
        ComponentId id,
        SemanticVersion version,
        ComponentKind kind,
        String displayName,
        String description,
        List<PortDescriptor> inputs,
        List<PortDescriptor> outputs,
        String configurationSchemaRef,
        ComponentExecutionCapabilities execution,
        Map<String, String> metadata) {
    public ComponentDescriptor {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(kind, "kind");
        displayName = PortDescriptor.requireText(displayName, "displayName");
        description = PortDescriptor.optionalText(description);
        inputs = List.copyOf(Objects.requireNonNull(inputs, "inputs"));
        outputs = List.copyOf(Objects.requireNonNull(outputs, "outputs"));
        configurationSchemaRef = PortDescriptor.optionalText(configurationSchemaRef);
        Objects.requireNonNull(execution, "execution");
        metadata = Map.copyOf(Objects.requireNonNull(metadata, "metadata"));
        ensureUniquePorts(inputs, "input");
        ensureUniquePorts(outputs, "output");
        metadata.forEach((key, value) -> {
            if (key == null || !key.contains(":") || key.isBlank())
                throw new IllegalArgumentException("Metadata keys must be namespaced");
            if (value == null) throw new IllegalArgumentException("Metadata values must not be null");
        });
    }
    private static void ensureUniquePorts(List<PortDescriptor> ports, String type) {
        long unique = ports.stream().map(PortDescriptor::name).distinct().count();
        if (unique != ports.size()) throw new IllegalArgumentException("Duplicate " + type + " port name");
    }
}
