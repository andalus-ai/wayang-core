package tech.kayys.wayang.core.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Immutable plugin package metadata. It is a declaration, not proof of trust or permission grant. */
public record PluginDescriptor(
        PluginId id,
        SemanticVersion version,
        String publisher,
        String displayName,
        String description,
        String license,
        String pluginApiRange,
        String runtimeRange,
        List<ComponentDescriptor> components,
        List<PluginDependency> dependencies,
        Set<String> requestedPermissions,
        String artifactDigest,
        Map<String, String> metadata) {
    public PluginDescriptor {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(version, "version");
        publisher = PortDescriptor.requireText(publisher, "publisher");
        displayName = PortDescriptor.requireText(displayName, "displayName");
        description = PortDescriptor.optionalText(description);
        license = PortDescriptor.optionalText(license);
        pluginApiRange = PortDescriptor.requireText(pluginApiRange, "pluginApiRange");
        runtimeRange = PortDescriptor.optionalText(runtimeRange);
        components = List.copyOf(Objects.requireNonNull(components, "components"));
        dependencies = List.copyOf(Objects.requireNonNull(dependencies, "dependencies"));
        requestedPermissions = Set.copyOf(Objects.requireNonNull(requestedPermissions, "requestedPermissions"));
        requestedPermissions.forEach(permission -> {
            if (permission == null || permission.isBlank())
                throw new IllegalArgumentException("Requested permissions must not be blank");
        });
        artifactDigest = PortDescriptor.optionalText(artifactDigest);
        if (artifactDigest != null && !artifactDigest.matches("sha256:[a-fA-F0-9]{64}"))
            throw new IllegalArgumentException("artifactDigest must use sha256:<64 hex characters>");
        metadata = Map.copyOf(Objects.requireNonNull(metadata, "metadata"));
        metadata.forEach((key, value) -> {
            if (key == null || !key.contains(":") || key.isBlank())
                throw new IllegalArgumentException("Metadata keys must be namespaced");
            if (value == null) throw new IllegalArgumentException("Metadata values must not be null");
        });
        long uniqueComponents = components.stream().map(ComponentDescriptor::id).distinct().count();
        if (uniqueComponents != components.size())
            throw new IllegalArgumentException("Plugin cannot contribute the same component ID more than once");
        long uniqueDependencies = dependencies.stream().map(PluginDependency::pluginId).distinct().count();
        if (uniqueDependencies != dependencies.size())
            throw new IllegalArgumentException("Plugin cannot declare the same dependency more than once");
        if (dependencies.stream().anyMatch(dependency -> dependency.pluginId().equals(id)))
            throw new IllegalArgumentException("Plugin cannot depend on itself");
    }
}
