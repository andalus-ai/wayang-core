package tech.kayys.wayang.core.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class PluginDescriptorTest {
    @Test void recordsRequestedPermissionsWithoutGrantingThem() {
        var plugin = new PluginDescriptor(PluginId.of("acme:csv"), SemanticVersion.parse("1.2.0"),
            "Acme", "CSV tools", null, "Apache-2.0", "^1.0", null, List.of(), List.of(),
            Set.of("filesystem:read"), null, Map.of());
        assertTrue(plugin.requestedPermissions().contains("filesystem:read"));
    }
    @Test void rejectsSelfDependency() {
        var id = PluginId.of("acme:csv");
        assertThrows(IllegalArgumentException.class, () -> new PluginDescriptor(id,
            SemanticVersion.parse("1.0.0"), "Acme", "CSV", null, null, "1.x", null, List.of(),
            List.of(new PluginDependency(id, "1.x", false)), Set.of(), null, Map.of()));
    }
    @Test void validatesArtifactDigestShape() {
        assertThrows(IllegalArgumentException.class, () -> new PluginDescriptor(PluginId.of("acme:csv"),
            SemanticVersion.parse("1.0.0"), "Acme", "CSV", null, null, "1.x", null, List.of(),
            List.of(), Set.of(), "sha256:bad", Map.of()));
    }
}
