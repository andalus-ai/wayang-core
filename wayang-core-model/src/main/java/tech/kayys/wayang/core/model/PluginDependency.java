package tech.kayys.wayang.core.model;

/** Dependency declaration; range syntax is intentionally opaque until compatibility grammar is agreed. */
public record PluginDependency(PluginId pluginId, String versionRange, boolean optional) {
    public PluginDependency {
        if (pluginId == null) throw new NullPointerException("pluginId");
        versionRange = PortDescriptor.requireText(versionRange, "versionRange");
    }
}
