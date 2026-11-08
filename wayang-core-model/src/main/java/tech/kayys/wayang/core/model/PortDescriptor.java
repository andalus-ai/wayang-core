package tech.kayys.wayang.core.model;

import java.util.Objects;

/** Named component port. Schema references are opaque, immutable identifiers/URIs at this layer. */
public record PortDescriptor(String name, String schemaRef, boolean required, String description) {
    public PortDescriptor {
        name = requireText(name, "name");
        schemaRef = requireText(schemaRef, "schemaRef");
        description = optionalText(description);
    }
    static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        String normalized = value.trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException(field + " must not be blank");
        return normalized;
    }
    static String optionalText(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
