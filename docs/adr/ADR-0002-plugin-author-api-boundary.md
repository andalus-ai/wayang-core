# ADR-0002: Separate the portable plugin author API from host discovery SPI

- **Status:** Proposed
- **Date:** 2026-11-08
- **Decision owners:** Wayang Core, Wayang runtime maintainers
- **Related:** ADR-0001 shared contract boundary; `docs/MODULE-STRATEGY.md`; Wayang `docs/COMPONENT-PLUGIN-CONTRACT.md`

## Context

Wayang needs an extensible plugin mechanism across standalone, embedded, distributed, microservice and cloud-native deployments. Wayang Core is shared by Wayang and Wayang Console and must remain host-neutral.

The current Wayang contract documentation identifies `WayangExtension`, `Extensions`, `ComponentExecutor`, `ComponentExecutorCatalog`, `ComponentRegistry`, `PluginManifest` and `PluginManifestValidator`. It describes `ServiceLoader` as startup/classpath discovery and explicitly defers package installation, signature verification, dependency installation, placement and dynamic loading.

The source audit has now traced the exact public contracts for `WayangExtension`, `Extensions`, `ComponentDescriptor`, `ComponentRegistry`, `ComponentExecutor`, `ComponentExecutorRegistry`, `ComponentExecutorCatalog` and `PluginManifest` (see `docs/inventory/CONTRACT-INVENTORY.md`). It confirms that `WayangExtension` is a broad host SPI discovered through startup `ServiceLoader`, while the component registry/catalog only resolve metadata and validate supplied executor bindings. Consumer usage sites, service registration files, manifest serialization and Console adapters remain untraced. This ADR therefore decides the boundary, not a drop-in replacement API.

## Decision

Separate three concerns:

1. **Portable plugin author API (Core):** stable metadata and contribution declarations that do not require Quarkus, CDI, Wayang runtime classes, Console services, a transport, or a specific execution engine.
2. **Host discovery/activation SPI (Wayang):** service loading, provider instantiation, lifecycle, validation orchestration, classloader/process management, executor binding, registry mutation and runtime activation.
3. **Portable package/schema contract (Core):** language-neutral manifest and component descriptors. Java API types are conveniences for Java authors, not the only way to author a plugin.

The first Java API slice should be deliberately narrow: a plugin metadata provider exposes one immutable `PluginDescriptor` (or an equivalent descriptor factory) from `wayang-core-model`. It must not expose executable runtime services.

Illustrative API shape (proposal only):

```java
package tech.kayys.wayang.core.plugin;

import tech.kayys.wayang.core.model.PluginDescriptor;

public interface PluginDescriptorProvider {
    PluginDescriptor descriptor();
}
```

This interface is not yet approved as a stable public API. Its naming, loading semantics, duplicate-provider behavior and relationship to the existing `WayangExtension` must be checked against actual source and usage before implementation.

## Responsibilities by layer

### Core author API

May contain:
- Plugin descriptor provider contract.
- Stable metadata and contribution declarations.
- Optional author-side validation helpers with no host lifecycle side effects.
- Contract version constants only where they have a single authoritative owner.

Must not contain:
- Executor interfaces tied to Wayang execution context.
- CDI scopes, Quarkus annotations, Arc APIs or build-time processors.
- Console registration, catalog, approval, persistence or distribution services.
- Runtime classloader, process/container management or worker placement.
- Workflow graph, BPMN/DMN/EIP execution semantics.
- Permission grants, credentials or secrets.

### Wayang host SPI

Remains responsible for:
- Discovering legacy `WayangExtension` providers and new Core metadata providers.
- Binding component IDs/versions to Wayang executor implementations.
- Supplying runtime contexts and host services.
- Validating extension contributions against runtime compatibility and installed registries.
- Lifecycle start, stop, drain, cancellation and resource cleanup.
- Deciding whether code is embedded, isolated-local or remote.
- Preserving legacy startup behavior during migration.

### Console

Consumes manifest and inventory metadata only. It must not instantiate Java plugin providers or execute plugin code. Central approval and distribution do not imply local activation.

## Compatibility and migration

1. Preserve existing `WayangExtension` and `ServiceLoader` behavior. The preferred first adapter is a Wayang-owned projection from legacy extension metadata into Core descriptors; do not require existing plugins to implement a second provider interface.
2. Do not move `ComponentExecutor`, `ComponentExecutorRegistry` or `ComponentExecutorCatalog` into Core as-is: they depend on Wayang execution request/result types and runtime binding semantics. A future portable worker protocol is a separate contract family.
3. Do not force existing plugins to implement a second provider interface in the first migration.
4. A Wayang adapter may derive Core descriptors from legacy extension metadata while old and new contracts coexist.
5. Avoid reflection-based magic and implicit classpath scanning beyond documented provider mechanisms.
6. Define duplicate plugin ID/version and duplicate component ID/version behavior explicitly (fail closed; never silently choose one).
7. Treat provider construction and all returned metadata as untrusted input until validated.
8. Preserve the current v1 manifest schema and IDs; Java descriptor serialization must not be inferred from record field names.

## Deployment implications

- **Packaged JVM / embedded:** host may discover providers at startup from an explicit classpath or package.
- **Quarkus native / build-time augmentation:** providers must be included and indexed at build time; do not promise arbitrary hot loading.
- **Isolated local:** a separate process boundary requires an explicit IPC protocol; Core Java interfaces do not cross it.
- **Remote / cloud-native:** use language-neutral manifests and the future worker protocol, not Java SPI.
- **Console offline:** an unavailable Console must not stop an already activated runtime from executing its local deployment.

## Consequences

Positive:
- Core remains reusable by Java, Python, Go and UI tooling.
- Wayang keeps control of execution lifecycle and engine integration.
- Console remains a control plane, not an executable plugin host.
- JVM class loading is not confused with portable distributed extension support.

Costs:
- Java metadata API alone does not execute a component; Wayang must provide the runtime binding SPI.
- Migration requires a compatibility adapter and tests.
- Separate contracts and package versions need a documented compatibility matrix.

## Open questions before implementation

- Exact source paths, signatures and consumers of the existing Wayang plugin interfaces.
- Whether legacy extensions expose descriptors directly or derive them from executor registrations.
- Existing plugin discovery error and ordering semantics.
- Whether a plugin may contribute zero components (for schema/provider-only plugins).
- Whether one provider instance represents one package or multiple package contributions.
- Which metadata fields are required for a Java authoring convenience API versus the manifest.
- Whether a Core author API is needed for non-Java authors at all (the language-neutral schema may be sufficient).

## Acceptance criteria

Do not implement a stable API until:
- C1 traces exact Java source and all relevant usage sites.
- Golden fixtures characterize existing manifest/descriptor interchange.
- A Wayang adapter design preserves existing startup behavior.
- API dependency graph is verified to remain free of host dependencies.
- Sample plugin compiles against the proposed API and a supported Wayang host.
- Runtime and Console maintainers agree on ownership and compatibility.
