package tech.kayys.wayang.core.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Stable host-neutral identifier. Accepts the legacy Wayang dotted/hyphenated
 * identifier form and the explicit namespace:local-name form proposed for new contracts.
 * It must never encode a Java class name or deployment address.
 */
public record NamespacedId(String value) implements Comparable<NamespacedId> {
    private static final Pattern LEGACY = Pattern.compile("[a-z][a-z0-9]*(?:[.-][a-z0-9]+)+");
    private static final Pattern NAMESPACED = Pattern.compile(
        "[a-z][a-z0-9]*(?:[.-][a-z0-9]+)*:[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*");

    public NamespacedId {
        Objects.requireNonNull(value, "value");
        value = value.trim();
        if (value.length() > 255 || !(LEGACY.matcher(value).matches() || NAMESPACED.matcher(value).matches())) {
            throw new IllegalArgumentException("Expected a lowercase Wayang ID (e.g. acme.csv-reader or acme:csv.reader)");
        }
    }

    public static NamespacedId of(String value) { return new NamespacedId(value); }\n\n    @Override public int compareTo(NamespacedId other) { return value.compareTo(other.value); }
    @Override public String toString() { return value; }
}
