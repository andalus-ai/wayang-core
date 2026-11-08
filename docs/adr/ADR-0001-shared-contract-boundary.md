# ADR-0001: Establish Wayang Core as the shared contract boundary

- **Status:** Proposed
- **Date:** 2026-11-08
- **Decision owners:** Wayang and Wayang Console maintainers

## Context

Wayang and Wayang Console are separate repositories and deployable products. They need to agree on component/plugin identity, manifests, capability metadata, runtime management messages, and worker invocation contracts. Duplicating those definitions causes drift; sharing application services would create unwanted coupling between Studio/runtime and the standalone control plane.

The repositories already have their own implementations and public/internal types. Extraction must therefore preserve existing API and wire behavior rather than assuming a clean-slate design.

## Decision

Create `andalus-ai/wayang-core` as a versioned library and contract source consumed by both products.

Core owns only stable reusable contracts, portable schemas, protocol definitions, compatibility rules, and SDK/testkit primitives. It is not a running service and is not an application backend.

- Wayang owns workflow semantics, authoring, compiler, runtime execution, placement, local plugin lifecycle, and runtime-side management agent.
- Wayang Console owns fleet registration, deployment orchestration, central plugin governance/distribution, persistence, audit, and management UI.
- Gamelan remains the durable workflow execution engine.
- Andalus remains the agent execution framework.
- Numou remains optional and independent.

Core must not depend on any of these products or engines.

## Consequences

### Positive
- One canonical definition for cross-product contracts.
- Independent implementation and release cadence for runtime and Console.
- Easier third-party plugin and worker SDK development.
- No need for Console to import workflow execution code.

### Costs and constraints
- Core artifacts require semantic versioning and a compatibility matrix.
- Consumer migration requires golden fixtures and integration tests.
- Additional release coordination is needed for protocol changes.
- Core must resist becoming a dumping ground for shared-looking application code.

## Alternatives considered

1. **Keep duplicated contracts in Wayang and Console.** Rejected because the same concepts and protocol messages can drift.
2. **Put all shared code in Wayang and make Console depend on Wayang.** Rejected because Console must remain standalone and should not inherit runtime/execution dependencies.
3. **Put Console services in Core.** Rejected because Core is a library boundary, not a service or control plane.
4. **Make Core a mandatory online service.** Rejected because runtime execution must continue without Console or a central contract service.

## Migration constraints

1. Inventory actual source types and their consumers before extraction.
2. Capture current serialization and wire behavior in fixtures.
3. Extract stable data contracts before host-specific interfaces.
4. Keep persistence entities, REST resources, application services, and runtime implementations in their owning repositories.
5. Retain deprecated facades/adapters during migration where needed.
6. Publish artifacts only after build, schema, compatibility, and consumer tests pass.

## Follow-up decisions

- Select Java baseline and artifact coordinates after inspecting both consumer builds.
- Choose schema/protocol source tooling after inventorying existing JSON Schema and protobuf usage.
- Define version compatibility policy and release train.
- Decide whether management and worker contracts are JSON, protobuf, or a mixed set based on current transports and external SDK needs.
- Define plugin signing/trust metadata separately from authorization policy.
