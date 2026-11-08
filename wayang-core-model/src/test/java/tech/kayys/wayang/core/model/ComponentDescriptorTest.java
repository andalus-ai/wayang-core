package tech.kayys.wayang.core.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ComponentDescriptorTest {
    @Test void createsImmutableDescriptorWithOpenKindVocabulary() {
        var descriptor = new ComponentDescriptor(ComponentId.of("acme:csv.reader"),
            SemanticVersion.parse("1.0.0"), new ComponentKind("custom.reader"), "CSV Reader", null,
            List.of(new PortDescriptor("source", "schema:uri", true, null)), List.of(), null,
            new ComponentExecutionCapabilities(Set.of(ExecutionMode.EMBEDDED), StateModel.STATELESS, false, true, true),
            Map.of("acme:category", "data"));
        assertEquals("acme:csv.reader", descriptor.id().toString());
        assertThrows(UnsupportedOperationException.class, () -> descriptor.metadata().put("x:y", "z"));
    }
    @Test void rejectsRemoteModeWithoutPortablePayloads() {
        assertThrows(IllegalArgumentException.class, () -> new ComponentExecutionCapabilities(
            Set.of(ExecutionMode.REMOTE), StateModel.STATELESS, false, true, true));
    }
    @Test void rejectsDuplicatePortNames() {
        var port = new PortDescriptor("input", "schema:x", true, null);
        assertThrows(IllegalArgumentException.class, () -> new ComponentDescriptor(ComponentId.of("acme:x"),
            SemanticVersion.parse("1.0.0"), ComponentKind.ACTION, "X", null, List.of(port, port), List.of(),
            null, new ComponentExecutionCapabilities(Set.of(ExecutionMode.EMBEDDED), StateModel.STATELESS, false, false, false), Map.of()));
    }
}
