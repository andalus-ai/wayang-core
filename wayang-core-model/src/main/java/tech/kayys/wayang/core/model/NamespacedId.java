package tech.kayys.wayang.core.model;

import java.util.Objects;
import java.util.regex.Pattern;

/** Stable, host-neutral identifier. It must never encode a class name or deployment address. */
public record NamespacedId(String value) implements Comparable<NamespacedId> {
    private static final Pattern FORMAT = Pattern.compile("[a-z][a-z0-9]*(?:[.-][a-z0-9]+)*(?::[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*)+");

    public NamespacedId {
        Objects.requireNonNull(value, "value");
        value = value.trim();
        if (value.length() > 255 || !FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("Expected a lowercase namespaced identifier, e.g. acme:transform.csv");
        }
    }

    @Override public int compareTo(NamespacedId other) { return value.compareTo(other.value); }
    @Override public String toString() { return value; }
}
