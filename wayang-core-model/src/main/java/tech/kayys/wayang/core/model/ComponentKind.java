package tech.kayys.wayang.core.model;

import java.util.Objects;
import java.util.regex.Pattern;

/** Open vocabulary token: new kinds can be introduced without changing this library's enum ABI. */
public record ComponentKind(String value) {
    private static final Pattern FORMAT = Pattern.compile("[a-z][a-z0-9]*(?:[._-][a-z0-9]+)*");
    public ComponentKind {
        Objects.requireNonNull(value, "value");
        value = value.trim();
        if (!FORMAT.matcher(value).matches()) throw new IllegalArgumentException("Invalid component kind: " + value);
    }
    public static final ComponentKind ACTION = new ComponentKind("action");
    public static final ComponentKind TRIGGER = new ComponentKind("trigger");
    public static final ComponentKind CONDITION = new ComponentKind("condition");
    public static final ComponentKind TRANSFORM = new ComponentKind("transform");
    @Override public String toString() { return value; }
}
