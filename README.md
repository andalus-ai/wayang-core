# Wayang Core

Shared, versioned contracts and reusable libraries for the Wayang ecosystem.

This repository is the dependency foundation for:

- [Wayang](https://github.com/andalus-ai/wayang): Studio-facing authoring, validation, compilation, packaging, runtime integration, and local plugin execution.
- [Wayang Console](https://github.com/andalus-ai/wayang-console): standalone management/control plane for Wayang runtime fleets, deployments, and central plugin governance.

Wayang Core is a library and contract repository. It is **not** a workflow engine, runtime distribution, Studio, Console server, plugin marketplace, or deployment control plane.

## Ownership boundaries

| Concern | Owner |
|---|---|
| Stable component, node, connector, extension, and plugin contracts | Wayang Core |
| Portable schemas, manifests, protocol definitions, compatibility rules | Wayang Core |
| Reusable SDK primitives and contract test fixtures | Wayang Core |
| Workflow authoring, BPMN/DMN/EIP/agent composition, validation and compilation | Wayang |
| Runtime execution adapters, placement decisions, embedded plugin loading, worker hosting | Wayang |
| Runtime enrollment, fleet registry, deployment orchestration, central plugin approval/distribution, audit and digital-twin projections | Wayang Console |
| Durable workflow execution and run lifecycle | Gamelan |
| Agent execution and agent lifecycle | Andalus |

## Dependency direction

Wayang Core has no dependency on Wayang, Wayang Console, Gamelan, Andalus, Numou, Quarkus, a database, or a specific transport implementation.

Wayang and Wayang Console depend on compatible published Core artifacts. Core must never depend back on either application. Runtime and Console may implement different adapters for the same contract.

## Initial contract families

1. **Component model** — stable component identity, semantic version, kind, ports, configuration schema, capabilities, dependencies, and presentation-neutral metadata.
2. **Plugin model** — plugin identity, publisher, plugin/API compatibility, contributed component inventory, dependency declarations, permissions, provenance, and lifecycle capability declarations.
3. **Extension SPI** — Java extension points for trusted in-process implementations. The SPI is not itself a sandbox or a remote execution protocol.
4. **Portable manifests and schemas** — JSON Schema and language-neutral data definitions usable by Java, Flutter, Python, Go, and external tooling.
5. **Management and worker protocol contracts** — versioned wire schemas for runtime registration/inventory/commands and component worker invocation where appropriate.
6. **Compatibility and conformance** — compatibility policy, test fixtures, and testkit utilities for implementers.

## Design rules

- Keep public contracts transport-neutral unless the artifact is explicitly a protocol module.
- Keep domain contracts free of Quarkus, persistence, UI framework, and vendor-specific types.
- Use stable namespaced IDs and explicit semantic versions; never use Java class names as public component identity.
- Separate metadata discovery from executable code loading.
- A plugin package may contribute many components, nodes, connectors, triggers, providers, schemas, and UI metadata.
- A node is a workflow-facing representation; it is not necessarily a separate executable component.
- A connector is an integration implementation; it is not synonymous with a plugin.
- Treat manifests, package contents, and remote worker responses as untrusted input.
- Keep permissions explicit and least-privilege. A manifest declaration is not an authorization grant.
- Do not imply that dynamic JVM loading works in Quarkus native images or that classloader unload is guaranteed.
- Remote execution requires language-neutral serialization, authentication, deadlines, cancellation, idempotency, bounded payloads, error mapping, and compatibility negotiation.
- Preserve engine neutrality: Gamelan and Andalus are adapters/integrations, not dependencies of the shared component model.
- Keep Community deployments complete and usable without Wayang Console or Numou.

## Repository status

This repository has just been initialized as the shared architecture and contract boundary. The documents describe the intended target and migration plan. They do not claim that all listed APIs, Maven artifacts, protocol schemas, or SDKs have been implemented or released.

Start with [architecture](docs/ARCHITECTURE.md), [module strategy](docs/MODULE-STRATEGY.md), and [roadmap](docs/ROADMAP.md).
