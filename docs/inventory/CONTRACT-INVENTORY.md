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

