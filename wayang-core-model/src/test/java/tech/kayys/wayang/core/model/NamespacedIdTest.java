package tech.kayys.wayang.core.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NamespacedIdTest {
    @Test void acceptsLegacyManifestIdentifierSyntax() {
        assertEquals("acme.csv-reader", NamespacedId.of("acme.csv-reader").toString());
    }
    @Test void acceptsExplicitNamespacedIdentifierSyntax() {
        assertEquals("acme:csv.reader", NamespacedId.of("acme:csv.reader").toString());
    }
    @Test void rejectsUppercaseAndWhitespaceWithinIdentifier() {
        assertThrows(IllegalArgumentException.class, () -> NamespacedId.of("Acme:CSV"));
        assertThrows(IllegalArgumentException.class, () -> NamespacedId.of("acme:csv reader"));
    }
}
