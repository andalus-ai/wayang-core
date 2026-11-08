# Wayang Core module strategy

## Guiding principle
Create shared artifacts only for contracts or libraries with at least two real consumers or a clear external SDK use case. A separate Git repository does not require many Maven modules on day one.

## Initial logical layout
- wayang-core-model
- wayang-core-schema
- wayang-plugin-api
- wayang-plugin-spi
- wayang-management-contract
- wayang-worker-contract
- wayang-core-testkit
- wayang-core-bom
- docs

This is a target layout. The bootstrap repository does not yet claim these modules exist.

## Responsibilities

### Core model
Stable immutable value types: namespaced identifiers, semantic versions, component kind/descriptor/dependency/port/capability metadata, plugin identity/publisher/compatibility/contribution inventory, shared error codes and capability vocabulary.

Do not place workflow graph execution classes, JPA entities, REST resources, Quarkus annotations, or UI framework classes here.

### Schema
Plugin manifest JSON Schema; component descriptor/configuration interchange schema; deployment metadata and lockfile schemas only when shared by Studio/compiler and Console; canonical examples, fixtures and schema compatibility tests. Schemas are authoritative interchange definitions; generated Java classes are implementation conveniences.

### Plugin API and SPI
Keep public plugin authoring API separate from host-side discovery/management SPI. API is what plugin authors implement or declare. SPI is what a Wayang host uses to discover, validate and activate contributions. Shared SPI must not expose Console database services or Wayang internal execution engine.

### Management contract
Canonical protocol messages for enrollment/registration and identity; heartbeat/lease/health; component and workflow inventory; typed command polling/acknowledgement; deployment status and runtime events as those slices become supported. Console persistence and services stay in Console. Runtime agent implementation stays in Wayang.

### Worker contract
Language-neutral invocation/result/error envelope with invocation ID, component ID/version, contract version, workflow/run/node correlation and trace context, typed payload, deadline, cancellation, idempotency semantics, bounded payload metadata, worker capabilities/health and graceful drain.

Do not reuse the management command protocol as an execution protocol.

### Testkit
Golden JSON/protobuf fixtures, schema conformance tests, compatibility utilities, and minimal plugin/worker examples. Keep testkit dependencies test-scoped and avoid forcing a test framework on consumers.

## Dependency graph
Core model is the base for API, SPI, management and worker contracts. Schema describes portable interchange. Testkit consumes schemas and protocol fixtures. Wayang consumes model, plugin API, management and worker contracts. Console consumes model and management contract. BOM aligns versions without forcing every consumer to import every artifact.

## Keep outside Core
| Type | Owner |
|---|---|
| Workflow graph, BPMN/DMN/EIP semantic model and compiler | Wayang |
| Component execution engine, executor registry implementation, placement router | Wayang |
| Quarkus runtime bootstrap, CDI discovery, native-image integration | Wayang |
| Plugin installation orchestration and local classloader/process management | Wayang |
| Fleet registry persistence, enrollment service, deployment state machine | Console |
| Console REST resources, OIDC principal, repositories and JPA entities | Console |
| Console UI and Studio UI | Their respective applications |
| Gamelan execution state and scheduler | Gamelan |
| Andalus agent internals | Andalus |

## Build and release
Use a BOM to align versions but do not force every consumer to import every artifact. Keep schema and protocol compatibility independently versioned from implementation libraries. Prefer released artifacts for cross-repository consumption; avoid depending on another repository main branch or source tree. Publish sources, Javadocs, checksums, SBOM and provenance. Keep Java baseline explicit and aligned with supported consumer JDKs. Avoid Quarkus BOM imports in neutral Core model artifacts.
