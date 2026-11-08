package tech.kayys.wayang.core.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SemanticVersionTest {
    @Test void parsesAndPreservesBuildMetadata() {
        assertEquals("1.2.3-rc.1+build.7", SemanticVersion.parse("1.2.3-rc.1+build.7").toString());
    }
    @Test void releaseHasHigherPrecedenceThanPrerelease() {
        assertTrue(SemanticVersion.parse("1.0.0").compareTo(SemanticVersion.parse("1.0.0-rc.1")) > 0);
    }
    @Test void comparesNumericPrereleaseIdentifiersNumerically() {
        assertTrue(SemanticVersion.parse("1.0.0-rc.10").compareTo(SemanticVersion.parse("1.0.0-rc.2")) > 0);
    }
    @Test void rejectsLeadingZeroNumericPrerelease() {
        assertThrows(IllegalArgumentException.class, () -> SemanticVersion.parse("1.0.0-rc.01"));
    }
}
