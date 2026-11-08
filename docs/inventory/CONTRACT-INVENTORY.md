# C1 — Existing contract inventory

- **Status:** Source audit in progress; Wayang runtime SPI and executor-binding contracts traced, but consumer usages, manifest serialization and Console adapters remain open.
- **Reviewed:** 2026-11-08
- **Repositories:** `andalus-ai/wayang`, `andalus-ai/wayang-console`

This document records existing source-of-truth candidates before extraction. It deliberately does not claim that a Core artifact exists or that these types have already migrated.

## Wayang source inventory

| Existing contract or mechanism | Current location/source | Classification | Extraction decision |
|---|---|---|---|
| `ComponentDescriptor` | Wayang runtime component model (referenced by current contract docs) | Shared candidate | Extract stable identity, version, kind, ports, config-schema reference and capability metadata only after source/API inventory |
| `ComponentRegistry` | Wayang runtime | Wayang implementation | Keep registry implementation and lookup behavior in Wayang |
| `ComponentExecutor` / `ComponentExecutorCatalog` | Wayang runtime | Wayang implementation + API boundary candidate | Keep execution dispatch/catalog implementation local; extract only host-neutral SPI if a plugin author needs it |
| `WayangExtension` / `Extensions` | Wayang runtime | Wayang host SPI | Keep runtime bootstrap and discovery host-specific; consider a minimal author-facing API in Core |
| `ComponentExecutionCapabilities` / registry | Wayang runtime contract documented in COMPONENT-PLUGIN-CONTRACT.md | Shared metadata candidate + Wayang validation implementation | Extract immutable capability value model; keep registry and validation lifecycle in Wayang |
| `PluginManifest` / validator | Wayang runtime contract documented in COMPONENT-PLUGIN-CONTRACT.md | Shared contract candidate + Wayang validator | Extract manifest model/schema; keep parsing, validation orchestration, trust checks and installation services in Wayang |
| Plugin JSON Schema | `wayang-runtime/src/main/resources/schema/wayang-plugin-manifest.schema.json` | Canonical schema candidate | Move canonical source only after compatibility fixtures; preserve current schema ID/version during migration |
| `ServiceLoader` startup discovery | Wayang runtime | Wayang implementation | Keep; do not misrepresent as hot install/uninstall |

### Source-backed observations

- Wayang root Maven coordinates are `tech.kayys.wayang:wayang-parent:0.0.1`.
- The parent currently declares Java release 21 and Quarkus platform 3.32.2.
- The root reactor includes workflow/domain modules such as expression, CEL, DMN, business-process, integration, agent, runtime, connectors, publishing and adapters.
- `wayang-runtime` depends on Wayang expression, DMN, business-process, integration and agent modules. This confirms it is not an appropriate neutral Core dependency.
- The current plugin manifest schema uses JSON Schema 2020-12, API version shaped as major.minor, a namespaced plugin ID, SemVer plugin/component versions, publisher, runtime compatibility, component inventory, dependencies and permission strings.
- The current contract documentation explicitly states that package parsing, signature verification, dependency installation, placement and dynamic loading remain future work. `ServiceLoader` is startup/classpath discovery.

## Wayang Console source inventory

| Existing contract or mechanism | Current location/source | Classification | Extraction decision |
|---|---|---|---|
| Runtime management protobuf | `contracts/proto/wayang/management/v1/management.proto` | Shared wire contract | Candidate for canonical ownership in Core after golden wire fixtures |
| Runtime identity and registration messages | Management protobuf | Shared wire contract | Keep message semantics/field numbers stable; Console registration services stay local |
| Heartbeat and lease messages | Management protobuf | Shared wire contract | Shared schema; lease storage and policy stay in Console |
| Component capability and workflow inventory messages | Management protobuf | Shared wire contract | Share wire DTOs; Console inventory projections/persistence stay local |
| Poll/ack management command messages | Management protobuf | Shared wire contract | Share envelope; command catalog, authorization, queue and delivery stay local |
| Runtime registry, deployment, audit and plugin governance services | Console application | Console implementation | Do not move into Core |

### Current management protocol facts

The inspected protobuf declares package `wayang.management.v1`, Java package `ai.wayang.console.contracts.management.v1`, and service `RuntimeManagement`. RPCs are Register, Heartbeat, ReportInventory, PollCommands and AcknowledgeCommand.

It carries runtime identity, runtime/deployment/environment versions, supported protocol versions, registration lease, heartbeat interval, component capabilities, workflow references, typed command payload bytes, idempotency key, expiry, and command acknowledgement status.

**Compatibility constraint:** do not change field numbers, field types, package name or RPC semantics as part of initial extraction. First add golden serialization fixtures and runtime/Console interoperability tests. Review Java package naming separately; changing generated package names is not wire-compatible from a source/binary perspective even if protobuf wire format remains the same.

## Ownership classification

| Category | Owner |
|---|---|
| Component/plugin IDs, versions, descriptors, manifest schemas, capability metadata | Wayang Core (after extraction) |
| Plugin author API that is host-neutral | Wayang Core (minimal surface) |
| Runtime extension discovery, registry, validation orchestration, activation and execution | Wayang |
| Workflow graph, BPMN/DMN/EIP/agent semantics, compiler and packaging | Wayang |
| Runtime management agent and worker host adapters | Wayang |
| Management protobuf canonical schema (after fixture-backed migration) | Wayang Core |
| Runtime enrollment, lease persistence, fleet registry, command queue and deployment orchestration | Wayang Console |
| Console REST DTOs, database entities, repositories, operator policy and UI | Wayang Console |
| Durable workflow lifecycle | Gamelan |
| Agent runtime and lifecycle | Andalus |

## Open inventory questions

- [ ] Locate and inspect exact Java source paths and public method signatures for all Wayang component/plugin types.
- [ ] Trace all usages from public APIs, examples and tests before deciding whether an interface belongs in Core.
- [ ] Inspect Console generated protobuf configuration and runtime client/server adapters.
- [ ] Confirm Console Java/JDK baseline and build system before defining shared artifact compatibility.
- [ ] Determine whether Console actually consumes component/plugin manifest types today or only the management inventory projection.
- [ ] Add golden JSON fixtures for current manifest and descriptor serialization.
- [ ] Add golden protobuf fixtures for every current management request/response and command envelope.
- [ ] Record license and artifact publication constraints before publishing SDK artifacts.

## C1 exit criteria

C1 is complete only when all open questions above are resolved or explicitly deferred with rationale, source usages are traced, and golden fixtures cover current JSON/protobuf interchange. This initial inventory is a starting point, not a completed extraction audit.


## Source audit — Wayang runtime plugin and execution boundary

Reviewed directly from the Wayang `main` source tree on 2026-11-08. These observations are source-backed and supersede the earlier path-level placeholders above.

| Type | Exact source path | Observed public contract / behavior | Ownership |
|---|---|---|---|
| `WayangExtension` | `wayang-runtime/src/main/java/tech/kayys/wayang/runtime/WayangExtension.java` | Interface with `id()`, profile `requires()`, and default contribution methods for connectors, pollers, webhook providers, domain handlers, component descriptors and execution capabilities | Wayang host SPI |
| `Extensions` | `wayang-runtime/src/main/java/tech/kayys/wayang/runtime/Extensions.java` | `load(Profile)` delegates to `load(ClassLoader, Profile)`; uses `ServiceLoader.load(WayangExtension.class, cl)`; `of(...)` rejects duplicate extension IDs and filters by profile features | Wayang discovery/bootstrap |
| `ComponentDescriptor` | `wayang-runtime/src/main/java/tech/kayys/wayang/runtime/ComponentDescriptor.java` | Java record carrying namespaced ID, version, kind, executor type, description, required features, dependencies, isolation level, capabilities, display name, category, icon, config schema and UI hints; retains a source-compatible 9-argument constructor | Shared metadata candidate; do not move runtime enum dependencies blindly |
| `ComponentRegistry` | `wayang-runtime/src/main/java/tech/kayys/wayang/runtime/ComponentRegistry.java` | Profile-scoped read-only registry; merges built-ins and extension descriptors; rejects duplicate IDs; resolves deterministic dependency-first closure and rejects unavailable/incompatible dependencies and cycles | Wayang implementation |
| `ComponentExecutor` | `wayang-runtime/src/main/java/tech/kayys/wayang/runtime/ComponentExecutor.java` | `executorId()` and `execute(ExecutionRequest)`; documents that runtime retry, timeout, cancellation and persistence policy are not delegated to the executor | Wayang execution SPI |
| `ComponentExecutorRegistry` | `wayang-runtime/src/main/java/tech/kayys/wayang/runtime/ComponentExecutorRegistry.java` | Immutable component-ID-to-executor dispatch map; missing executor returns a rejected result | Wayang implementation |
| `ComponentExecutorCatalog` | `wayang-runtime/src/main/java/tech/kayys/wayang/runtime/ComponentExecutorCatalog.java` | Resolves component closure, then requires exactly one binding per resolved component and validates component version plus executor family; rejects extra, duplicate and missing bindings; explicitly does not load classes or install artifacts | Wayang release/runtime validation |
| `PluginManifest` | `wayang-runtime/src/main/java/tech/kayys/wayang/runtime/PluginManifest.java` | Immutable record with namespaced plugin ID, SemVer version, major.minor API version, publisher, runtime compatibility, non-empty component list, plugin dependencies, permission set and optional license; nested provided-component and dependency records | Shared manifest candidate; retain Wayang validator/orchestration |
| `ComponentExecutionCapabilities` | `wayang-runtime/src/main/java/tech/kayys/wayang/runtime/ComponentExecutionCapabilities.java` | Runtime deployment capability declaration; inspect the complete type and validator before extracting | Candidate value model; validation stays in Wayang |

### Concrete architectural consequences

1. **Do not replace `WayangExtension` with `PluginDescriptorProvider`.** The former is a broad host SPI with executable handler/provider contributions; the latter is only a proposed metadata author API. They are not equivalent.
2. **Do not move `ComponentExecutor`, `ComponentExecutorRegistry` or `ComponentExecutorCatalog` into Core as-is.** They depend on Wayang execution request/result types and host semantics. A future portable worker protocol should be a separate language-neutral contract, not a renamed Java executor interface.
3. **The current metadata registry is not a plugin manager.** `ComponentRegistry` resolves metadata and dependency closure; `ComponentExecutorCatalog` validates already-supplied bindings. Neither installs artifacts, creates classloaders, starts isolated processes or places remote workers.
4. **Current ServiceLoader behavior is startup discovery.** `Extensions.load` creates a deployment snapshot. It is not evidence of safe runtime hot install/uninstall, nor does it establish Quarkus native support for arbitrary runtime providers.
5. **Preserve the legacy extension API during migration.** The safe first adapter direction is legacy `WayangExtension` -> Core metadata projection, with host-owned execution handlers remaining attached to the legacy extension. Do not require existing extensions to implement a second provider interface.
6. **Keep deployment mode explicit.** The existing descriptor has isolation levels `IN_PROCESS`, `SEPARATE_PROCESS`, `CONTAINER` and `REMOTE`; the separate capability model should be audited before mapping these to embedded, isolated-local, distributed and cloud-native execution.

### Source paths inspected

- `wayang-runtime/src/main/java/tech/kayys/wayang/runtime/WayangExtension.java`
- `wayang-runtime/src/main/java/tech/kayys/wayang/runtime/Extensions.java`
- `wayang-runtime/src/main/java/tech/kayys/wayang/runtime/ComponentDescriptor.java`
- `wayang-runtime/src/main/java/tech/kayys/wayang/runtime/ComponentRegistry.java`
- `wayang-runtime/src/main/java/tech/kayys/wayang/runtime/ComponentExecutor.java`
- `wayang-runtime/src/main/java/tech/kayys/wayang/runtime/ComponentExecutorRegistry.java`
- `wayang-runtime/src/main/java/tech/kayys/wayang/runtime/ComponentExecutorCatalog.java`
- `wayang-runtime/src/main/java/tech/kayys/wayang/runtime/PluginManifest.java`

### Remaining audit gaps

- [ ] Trace every construction and consumer of `WayangExtension`, `Extensions`, `ComponentDescriptor` and executor bindings across runtime, examples and tests.
- [ ] Inspect `ComponentExecutionCapabilities` and its registry/validator end-to-end.
- [ ] Inspect manifest parser/validator and JSON serialization implementation; do not infer JSON shape from Java record fields.
- [ ] Inspect `META-INF/services` registrations and packaging behavior.
- [ ] Inspect Console management client/server adapters and generated protobuf configuration.
- [ ] Add characterization fixtures before declaring the Core schema or Java model wire-compatible.



## Follow-up audit — capabilities, manifest validation and real provider registration

The repository's recursive Git tree and targeted source/test inspection found the following additional evidence.

### Execution capability contract

- `wayang-runtime/src/main/java/tech/kayys/wayang/runtime/ComponentExecutionCapabilities.java` declares an exact component ID/version, supported execution modes, state model, portable-payload flag, cancellation/idempotency flags, optional affinity key, required capabilities and resource hints.
- Its constructor rejects empty modes, requires an affinity key only for `AFFINE` state, and requires portable payloads for `REMOTE` and `CLOUD_NATIVE`.
- `ComponentExecutionCapabilityRegistry.of(...)` verifies that every declaration references an available component at the exact version and rejects duplicate declarations.
- Missing declarations preserve legacy embedded behavior only; they do not imply remote support. This is explicitly covered by `ComponentExecutionCapabilitiesTest`.

**Extraction implication:** the immutable declaration is a strong Core model candidate. The registry's relationship to Wayang's profile-scoped component catalog remains host behavior. Keep the distinction between declared capability and authorization/placement decision.

### Manifest validation and schema mapping

- `PluginManifestValidator.validateComponents(...)` compares the manifest's declared component ID/version set with supplied `ComponentDescriptor` values, rejects duplicates and mismatches, and returns the supplied descriptors.
- The validator explicitly documents that this is a consistency check, not signature verification.
- `wayang-runtime/src/main/resources/schema/wayang-plugin-manifest.schema.json` defines JSON Schema Draft 2020-12 with ID `https://wayang.ai/schemas/plugin-manifest-1.0.json`. Its wire shape is nested as `apiVersion`, `kind`, `metadata` and `spec`.
- The Java `PluginManifest` record is a flattened in-memory model with fields such as `id`, `version`, `publisher`, `runtimeCompatibility`, `components`, `dependencies` and `permissions`.

**Critical compatibility finding:** the Java record field layout is not the JSON schema layout. Do not serialize the record directly by field name and assume it implements the manifest schema. Core extraction must preserve the schema as canonical and define/test an explicit mapper. Do not change the existing schema ID or nesting as part of a refactor.

### Concrete ServiceLoader provider

- `wayang-multimodal/src/main/resources/META-INF/services/tech.kayys.wayang.runtime.WayangExtension` registers `tech.kayys.wayang.multimodal.MultimodalExtension`.
- `wayang-multimodal/src/main/java/tech/kayys/wayang/multimodal/MultimodalExtension.java` implements `WayangExtension`, requires the MEDIA profile feature, contributes connector implementations and contributes four component descriptors.
- Its connector contribution constructs `UrlGuard` and `MediaStore` using environment configuration. This is executable host contribution behavior, not pure metadata authoring.

This is direct evidence that `WayangExtension` cannot be reduced to a descriptor provider without losing runtime contributions. Any adapter must preserve connector/handler contributions and only project the metadata subset to Core.

### Characterization tests located

- `wayang-runtime/src/test/java/tech/kayys/wayang/runtime/PluginManifestTest.java` covers manifest validation, duplicate inventory, SemVer and permission validation, plus manifest-to-descriptor inventory consistency.
- `wayang-runtime/src/test/java/tech/kayys/wayang/runtime/ComponentRegistryTest.java` covers dependency closure, profile filtering, duplicate IDs, cycles, extension metadata contribution and built-in catalog composition.
- `wayang-runtime/src/test/java/tech/kayys/wayang/runtime/ComponentExecutorCatalogTest.java` covers exact executor binding, missing dependency executors, version/family mismatch and unselected/duplicate bindings.
- `wayang-runtime/src/test/java/tech/kayys/wayang/runtime/ComponentExecutionCapabilitiesTest.java` covers remote payload portability, affinity, exact component version, duplicate declarations and missing metadata behavior.

These are existing source tests, not tests run as part of this documentation update.

### Updated remaining audit gaps

- [ ] Trace every construction and consumer of extension/descriptor/executor types beyond the test coverage and multimodal provider identified above.
- [ ] Inspect the concrete manifest parser and serialization mapper (if present), plus the schema-validation invocation path.
- [ ] Inspect all service registration files and verify provider loading in packaged JVM and Quarkus native builds.
- [ ] Inspect Console management client/server adapters and generated protobuf configuration.
- [ ] Add JSON golden fixtures that prove the Java model maps to the nested v1 manifest schema.
- [ ] Add a Core/Wayang consumer compatibility test before extracting any public type.


## Follow-up audit — plugin package catalog and standalone Console protocol

### Existing Wayang plugin package registry

- `wayang-runtime/src/main/java/tech/kayys/wayang/runtime/PluginRegistry.java` is an immutable catalog over `PluginPackage` values. It indexes plugins and component ownership, rejects duplicate plugin IDs and component ownership conflicts, validates required/optional plugin dependencies and version constraints, and resolves requested plugins in deterministic dependency-first order.
- `PluginRegistry.PluginPackage` validates its component list against the manifest through `PluginManifestValidator.validateComponents`.
- Its bounded version-range implementation supports exact versions, wildcard, caret, tilde and numeric comparator conjunctions.
- `wayang-runtime/src/test/java/tech/kayys/wayang/runtime/PluginRegistryTest.java` covers dependency order, missing/incompatible and optional dependencies, cycles, duplicate plugin IDs, component ownership conflicts and unsupported range syntax.

**Boundary:** this is the catalog/validation layer, not artifact acquisition, signature verification, classloader creation, activation, process supervision or remote placement. Core should not duplicate it. If shared semantics are extracted later, move only portable manifest/package/dependency value contracts and keep this resolver/catalog host-side until cross-runtime use cases justify a shared resolver.

### Console remains a separate control plane

Inspected `andalus-ai/wayang-console`:

- `contracts/proto/wayang/management/v1/management.proto` defines `RuntimeManagement` RPCs: Register, Heartbeat, ReportInventory, PollCommands and AcknowledgeCommand. Its package and Java package are `wayang.management.v1` and `ai.wayang.console.contracts.management.v1`.
- The protobuf inventory model currently reports coarse `ComponentCapability(component_id, version, capability)` values and workflow references. It does not transfer plugin manifests, plugin artifacts, signatures, extension classes or activation requests.
- The backend implementation inspected is REST-based: `RuntimeRegistryResource` exposes enrollment, heartbeat and inventory endpoints; `RuntimeRegistryService` owns enrollment/registration credentials and leases; `RuntimeInventoryService` replaces the runtime's reported component capability and workflow inventory.
- `RuntimeInventoryService` validates required fields and caps each inventory collection at 1000 entries. The Console stores inventory as observed runtime capability data, not as an artifact installation catalog.

**Boundary:** Console is a standalone management/control plane. It can observe deployed runtime inventory and issue management commands, but the inspected inventory API is not a plugin installer. Plugin installation and activation remain runtime/deployment responsibilities. The protobuf contract and REST backend are distinct surfaces; do not claim the protobuf RPCs are wired to the REST implementation without verifying a gRPC server adapter.

### Manifest parsing and packaging findings

The recursive source tree contains the Java manifest model, validator, and JSON Schema, but no dedicated manifest parser/serializer class or explicit schema-validation invocation was found in the runtime source paths inspected. The manifest Java record and nested JSON Schema therefore remain separate artifacts until a concrete mapper is identified or added.

The repository has a concrete `META-INF/services/tech.kayys.wayang.runtime.WayangExtension` registration in `wayang-multimodal`. JVM ServiceLoader behavior is established by source. Quarkus native inclusion/registration behavior has not been verified and must be covered by native packaging tests or Quarkus build-time registration metadata.

### Updated migration decision

1. Keep `PluginRegistry`, `ComponentRegistry`, `ComponentExecutorCatalog`, and the `WayangExtension` SPI in Wayang runtime.
2. Extract only portable descriptor/manifest/capability value models and schema governance into Core, with explicit JSON mapping and golden fixtures.
3. Add a Wayang-owned projection adapter that converts legacy extension contributions into Core metadata while leaving executable connector/handler contributions attached to the original extension.
4. Keep Console management protocol independent from plugin author and worker execution protocols. Extend its inventory only when a concrete management use case requires richer plugin/package identity.
5. Do not implement hot install in Quarkus native by assuming runtime `ServiceLoader` can load arbitrary new classes. Runtime loading strategy must be selected per deployment mode: build-time packaged, JVM isolated classloader, separate process/container, or remote worker.

### C1 status

The principal public contracts, capability model, manifest validator, package registry, one concrete provider registration, relevant characterization tests, and Console management protocol/REST inventory boundary have now been traced. Remaining work is narrower: exhaustive runtime call-site tracing, identify any non-runtime parser/serializer or schema validation wiring, inspect generated protobuf build integration and any actual gRPC service implementation, and verify native packaging behavior. No code/test execution is claimed by this documentation update.

## Core model compatibility review — do not add a naive legacy mapper

The current Core Java model and proposed schemas were inspected after the Wayang source audit. They are intentionally richer and not structurally identical to the existing Wayang runtime types.

| Legacy Wayang source | Core candidate | Mapping status | Required treatment |
|---|---|---|---|
| PluginManifest | PluginDescriptor | Partial / lossy | Legacy manifest has API version, runtime compatibility, component inventory, dependencies, permissions and license. Core adds required display name, plugin API range, optional digest and namespaced metadata. Preserve original API version and nested v1 schema; add an explicit legacy projection policy for absent Core fields. |
| ComponentDescriptor | Core ComponentDescriptor | Semantic mismatch | Legacy has executor family, profile features, component dependencies, isolation level, capability strings, config schema and UI hints. Core has typed ports, configuration schema ref and typed execution capabilities, but does not currently model executor family, profile feature requirements, component dependency ranges or UI hints. Do not silently discard these fields during migration. |
| ComponentExecutionCapabilities | Core ComponentExecutionCapabilities | Partial | Core captures supported modes, state model, payload portability, cancellation and idempotency. Legacy additionally has affinity key, required capabilities and resource hints. Decide whether these are runtime scheduling metadata, portable descriptor extensions or intentionally host-only. |
| PluginRegistry | No direct Core equivalent | Intentionally host-owned | Keep dependency resolution, package catalog, component ownership, installed-state semantics and registry operations in Wayang. Core should own values/schema only. |
| WayangExtension | Proposed PluginDescriptorProvider | Not equivalent | Extension contributes executable connectors, pollers, webhook providers and handlers as well as metadata. Preserve host SPI and project only metadata. |

### Required mapping rules

- Treat the existing Wayang v1 plugin manifest as a stable external wire contract. Core's PluginDescriptor is not a replacement serialization model for it.
- Never invent Core displayName, plugin API range, artifact digest, ports, state model, or execution modes from unrelated legacy fields. A projection must use documented defaults or report that a field is unavailable.
- Preserve executor family, profile requirements, component dependencies, isolation level, UI hints, affinity and resource hints through a versioned extension field or a Wayang-owned sidecar until Core has explicit reviewed fields for them.
- Do not convert legacy isolation enum values directly to Core execution modes without a compatibility table and deployment semantics. For example, REMOTE is not automatically equivalent to CLOUD_NATIVE.
- Keep runtime live state (health, availability, worker load, observed deployment) out of static component descriptors and in Console/runtime inventory.
- Add round-trip and loss-reporting tests. The first adapter may be read-only projection for UI/catalog consumption; it must not claim lossless round-trip compatibility.

### C2/C3 implementation gate

Before a production adapter is merged:
1. Agree whether Core model is allowed to evolve to represent the missing stable legacy fields or whether they remain namespaced Wayang metadata.
2. Define required/optional/default behavior for every field in the mapping.
3. Add golden fixtures for the exact v1 manifest and a representative legacy component descriptor.
4. Add tests that prove no legacy field is silently lost, and that unsupported mappings fail or produce an explicit diagnostic.
5. Keep the adapter in a Wayang-owned module; Core must not depend on Wayang runtime classes.
