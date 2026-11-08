package tech.kayys.wayang.core.model;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Semantic Versioning 2.0.0 value with precedence comparison. */
public record SemanticVersion(int major, int minor, int patch, String preRelease, String build)
        implements Comparable<SemanticVersion> {
    private static final Pattern FORMAT = Pattern.compile(
        "(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-([0-9A-Za-z.-]+))?(?:\\+([0-9A-Za-z.-]+))?");

    public SemanticVersion {
        if (major < 0 || minor < 0 || patch < 0) throw new IllegalArgumentException("Version parts must be non-negative");
        preRelease = normalize(preRelease);
        build = normalize(build);
        validateIdentifiers(preRelease, true);
        validateIdentifiers(build, false);
    }

    public static SemanticVersion parse(String value) {
        Objects.requireNonNull(value, "value");
        Matcher matcher = FORMAT.matcher(value.trim());
        if (!matcher.matches()) throw new IllegalArgumentException("Invalid SemVer: " + value);
        try {
            return new SemanticVersion(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3)), matcher.group(4), matcher.group(5));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Version number exceeds supported integer range: " + value, ex);
        }
    }

    private static String normalize(String value) { return value == null || value.isBlank() ? null : value; }
    private static void validateIdentifiers(String value, boolean preRelease) {
        if (value == null) return;
        for (String part : value.split("\\.", -1)) {
            if (part.isEmpty() || !part.matches("[0-9A-Za-z-]+")
                || (preRelease && part.matches("0\\d+"))) {
                throw new IllegalArgumentException("Invalid SemVer identifier: " + part);
            }
        }
    }

    @Override public int compareTo(SemanticVersion other) {
        int c = Integer.compare(major, other.major); if (c != 0) return c;
        c = Integer.compare(minor, other.minor); if (c != 0) return c;
        c = Integer.compare(patch, other.patch); if (c != 0) return c;
        if (preRelease == null) return other.preRelease == null ? 0 : 1;
        if (other.preRelease == null) return -1;
        String[] left = preRelease.split("\\."), right = other.preRelease.split("\\.");
        for (int i = 0; i < Math.min(left.length, right.length); i++) {
            boolean ln = left[i].matches("\\d+"), rn = right[i].matches("\\d+");
            if (ln && rn) c = compareNumeric(left[i], right[i]);
            else if (ln != rn) c = ln ? -1 : 1;
            else c = left[i].compareTo(right[i]);
            if (c != 0) return c;
        }
        return Integer.compare(left.length, right.length);
    }

    private static int compareNumeric(String a, String b) {
        String x = a.replaceFirst("^0+(?!$)", ""), y = b.replaceFirst("^0+(?!$)", "");
        int c = Integer.compare(x.length(), y.length());
        return c != 0 ? c : x.compareTo(y);
    }

    @Override public String toString() {
        return major + "." + minor + "." + patch + (preRelease == null ? "" : "-" + preRelease)
            + (build == null ? "" : "+" + build);
    }
}
