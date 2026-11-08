# Wayang Core architecture

## Status
**Proposed shared architecture / repository bootstrap.** This repository establishes the ownership boundary and migration plan. Contract extraction, published artifacts, and consumer adoption are staged work. Existing Wayang contracts remain authoritative until compatibility-preserving migration is completed.

## Why a shared repository
Wayang and Wayang Console are separate products with different runtime responsibilities, but they must agree on component, plugin, runtime capability, package metadata, and management protocol semantics. A shared contract source prevents duplicated DTOs, enums, version parsing, manifest validation, and protobuf definitions from drifting.

Wayang Core is the canonical source for shared meaning and reusable contracts. It is not a shared application backend.

## System context
Wayang Studio/compiler, Wayang Runtime, and Wayang Console all depend on Wayang Core. Wayang Runtime connects to Gamelan for durable workflow execution and Andalus for agent execution. Wayang Console communicates with runtimes through versioned management protocols. Core is a compile-time/library dependency, not a mandatory online service.

## Ownership

### Wayang Core
Owns component and extension API contracts; plugin identity and manifest model; portable schemas; versioned management and worker protocol definitions; shared error taxonomy and capability vocabulary; compatibility policy; reusable SDK/testkit primitives.

Does not own workflow execution/scheduling, Studio services/UI, runtime plugin installation orchestration, Console database entities/repositories/API resources, marketplace operations, billing, tenant administration, or product-specific policy.

### Wayang
Owns BPMN, DMN, EIP, agent and human-task semantics; workflow model, validation, compiler, profiles and release packaging; component execution router and local runtime plugin manager; embedded, isolated-local, remote-worker and cloud worker adapters; runtime Management Agent; Studio APIs.

### Wayang Console
Owns runtime enrollment, identity, heartbeat and fleet registry; central plugin catalog/approval/distribution policy; deployment desired/observed state and reconciliation; management command persistence, authorization, audit and delivery; runtime inventory, digital twin, telemetry aggregation and Console UI.

Console consumes shared metadata and protocols; it does not load executable workflow handlers or become a workflow engine.

## Shared contracts and application adapters
A shared contract describes what applications agree on, not how either persists or executes it.

- Core defines plugin descriptors, manifest schema and API compatibility.
- Wayang implements local discovery, validation, loading and execution adapters.
- Console implements catalog persistence, approval, distribution, rollout and audit adapters.
- Core defines management wire messages.
- Wayang implements the runtime-side management agent.
- Console implements server-side endpoints and application services.

## Plugin vocabulary
- Component: versioned functional capability referenced by a workflow.
- Node: authoring/runtime graph representation of a component or native workflow construct.
- Connector: integration implementation for an external system or protocol.
- Extension: implementation contribution registered through a supported extension point.
- Plugin: distributable package and lifecycle boundary that may contribute multiple components, connectors, triggers, providers, schemas and presentation metadata.
- Worker: process/container/service hosting executable components.

A plugin may be loaded in-process only when trusted and compatible. Isolated or remote execution is a separate security and protocol boundary.

## Execution modes
The component contract may declare EMBEDDED, ISOLATED_LOCAL, REMOTE, or CLOUD_NATIVE support. These are capability declarations, not placement instructions. Wayang chooses placement using policy, compatibility, worker health, resources, security zone, data locality and state affinity. Console may govern allowed deployment targets but must not decide workflow semantics.

Quarkus build-time augmentation and native-image closed-world constraints mean arbitrary plugins cannot be assumed to appear in a running native executable. Support build-time packaging, optional JVM-only dynamic loading where safe, and remote workers as the portable extension path.

## Candidate contract families
These are logical boundaries, not a mandate to create every Maven module immediately.

| Logical artifact | Responsibility | Consumers |
|---|---|---|
| wayang-core-model | IDs, versions, component/plugin descriptors, capabilities | Wayang, Console, SDKs |
| wayang-core-schema | JSON Schema, manifest schema, examples and fixtures | Wayang, Console, tooling |
| wayang-plugin-api | Public extension-facing interfaces | Plugin authors, Wayang |
| wayang-plugin-spi | Host-side provider interfaces and discovery metadata | Wayang runtime |
| wayang-management-contract | Runtime enrollment, inventory, heartbeat, command and event protocol | Wayang, Console |
| wayang-worker-contract | Portable component invocation/result/error protocol | Wayang, worker SDKs |
| wayang-core-testkit | Contract fixtures and conformance helpers | Wayang, Console, plugin authors |
| wayang-core-bom | Compatible dependency versions | Consumers |

Only create separate artifacts when there is a real independent consumer or dependency boundary. Split by dependency direction, not class count.

## Dependency direction
- Wayang -> Core.
- Console -> Core.
- Plugin SDK -> Core API/schema.
- Runtime adapters -> Core plus host-specific dependencies.
- Core must never depend on Wayang, Console, Gamelan, Andalus, or Numou.

Never make Wayang depend on Console or Console depend on Wayang execution/runtime modules. Their shared code boundary is Core; network integration uses versioned management protocols.

## Versioning
- Version component IDs, plugin packages, plugin API, management protocol, and worker protocol explicitly.
- Do not conflate plugin version with component version or runtime distribution version.
- A runtime may support multiple compatible protocol versions during rolling upgrades.
- Schema evolution is additive within a major version; reserve protobuf field numbers and never reuse removed numbers.
- Compatibility checks include API version, runtime version range, component versions, dependency closure and required capabilities.
- Publish a compatibility matrix for each Core release train and verify both consumers against it.

## Migration strategy
1. Inventory existing contracts in Wayang and Console; identify semantic duplicates and application-specific types.
2. Freeze existing public shapes as compatibility baselines and add characterization/serialization tests.
3. Extract only genuinely shared, stable contracts without changing behavior.
4. Publish versioned Core artifacts and a BOM; retain deprecated compatibility facades where needed.
5. Update Wayang to consume Core first, then Console. Keep persistence entities and REST response models local unless they are actual wire contracts.
6. Move canonical protobuf/schema sources to Core; generate or consume server/client types from that source.
7. Add cross-repository contract tests and a release compatibility matrix.
8. Remove duplicate definitions only after both consumers migrate and tests prove wire compatibility.

Do not perform a wholesale package rename or move application services into Core in one change.

## Security
Treat plugin packages and manifests as untrusted. Verify artifact digest, publisher signature, compatibility, dependencies and policy before distribution/activation. Plugin permissions are declarations to be evaluated by the host, not self-granted capabilities. Never run untrusted third-party code in-process as a sandbox. Management commands are typed, authenticated, authorized, idempotent, expiring and auditable. Core contracts must not contain secrets or raw credential values.
