# C1 — Existing contract inventory

- **Status:** Initial source-backed inventory; incomplete until all Java implementations and consumers are traced.
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
