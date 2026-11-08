# Repository Agent Instructions

These instructions apply to the entire wayang-core repository.

## Purpose and ownership
- Own stable, reusable Wayang ecosystem contracts and libraries.
- This is not an application, workflow engine, Studio, runtime distribution, Console server, or marketplace.
- Wayang owns authoring, workflow semantics, compilation, packaging, runtime integration, and local execution.
- Wayang Console owns fleet management, deployment orchestration, central plugin approval/distribution, governance, and management UI.
- Gamelan owns durable workflow execution. Andalus owns agent execution. Keep both behind adapters.
- Numou is optional and independent. Core must not depend on Numou.

## Dependency rules
- Core must never depend on Wayang, Wayang Console, Gamelan, Andalus, Numou, Quarkus, a database, or a UI framework.
- Keep domain contracts free of transport and persistence implementation types.
- Keep Java SPI separate from language-neutral schemas and wire contracts.
- Do not move application services, repositories, entities, REST resources, UI code, runtime engines, or product-specific orchestration here.
- Avoid circular dependencies and application-to-application dependencies.

## Contract design
- Use stable namespaced IDs and explicit semantic versions.
- Separate component, node, connector, plugin, extension, and worker concepts.
- Separate metadata discovery from executable code loading.
- Use immutable value types where compatible with the selected Java baseline.
- Validate untrusted manifests and protocol payloads at boundaries; bound sizes and nesting.
- Do not use Java class names as public component identity.
- Do not expose Quarkus, Hibernate, Panache, Mutiny, or vendor-specific types from neutral domain APIs.
- Keep UI metadata renderer-neutral; never couple contracts to Flutter widgets.
- A manifest permission declaration is not an authorization grant.
- Do not promise safe dynamic classloader unloading or native-image hot installation without proven support.
- Remote invocation contracts must define serialization, authentication, authorization, deadlines, cancellation, idempotency, duplicate delivery, error mapping, compatibility negotiation, and resource limits.

## Compatibility and releases
- Version each independently consumable artifact and schema.
- Define compatibility policy before publishing stable APIs.
- Prefer additive evolution. Breaking changes require a major version or documented migration window.
- Maintain schema fixtures and conformance tests for every public interchange contract.
- Do not publish snapshot-only APIs as stable.
- Keep BOM and dependency versions aligned with supported Wayang and Console release trains.

## Implementation workflow
1. Inspect existing implementations in Wayang and Console before extracting or duplicating contracts.
2. Write an ADR for ownership, public API, module boundary, or compatibility decisions.
3. Implement the smallest coherent contract slice and tests.
4. Update docs, schemas, examples, and conformance fixtures together.
5. Distinguish proposed, implemented, tested, and released states.
6. Do not mark an item complete because an interface or schema exists.
7. Do not add Maven modules or dependencies without explaining why existing artifacts are insufficient.

## Verification
- Record exact build/test commands and outcomes.
- Distinguish compilation, unit tests, schema validation, compatibility tests, integration tests, and consumer verification.
- Do not claim Wayang or Console compatibility until their consumer builds/tests have actually run against candidate Core artifacts.
- If tooling or dependency resolution is unavailable, state the limitation.
