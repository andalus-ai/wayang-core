package tech.kayys.wayang.core.model;

/** Stable identity of a distributable plugin package. */
public record PluginId(NamespacedId id) {
    public PluginId { if (id == null) throw new NullPointerException("id"); }
    public static PluginId of(String value) { return new PluginId(new NamespacedId(value)); }
    @Override public String toString() { return id.toString(); }
}
