package tech.kayys.wayang.core.model;

/** Stable identity of a component capability, independent of its implementation or hosting location. */
public record ComponentId(NamespacedId id) {
    public ComponentId { if (id == null) throw new NullPointerException("id"); }
    public static ComponentId of(String value) { return new ComponentId(new NamespacedId(value)); }
    @Override public String toString() { return id.toString(); }
}
