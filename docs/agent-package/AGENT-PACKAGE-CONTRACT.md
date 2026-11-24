# Wayang Agent Package Contract

> **Status: proposed interchange contract, schema and examples added.** This does not implement marketplace publishing, artifact installation, signature verification, runtime activation, billing, or agent deployment.

## 1. Purpose

An Agent Package is an immutable, versioned, distributable artifact that describes a reusable custom agent and the requirements needed to install and invoke it.

It is not:
- an executable plugin or Java class;
- a running agent instance or conversation;
- a workflow definition;
- a deployment descriptor;
- a permission grant;
- a marketplace listing or publisher verification result.

The package contract enables a collaborator to publish an agent once and lets consumers install it into a project, invoke it independently, or compose it as a typed agent node in another workflow.

The canonical JSON Schema is `schemas/agent-package/v1/schema.json`. Positive and negative examples are in the adjacent `examples/` directory. The schema is an experimental contract until a schema validation/conformance pipeline and consumer adapters are implemented.

## 2. Keep the contracts separate

| Concern | Contract / owner | Responsibility |
|---|---|---|
| Agent behavior definition | Wayang `AgentSpec` and future portable agent definition | Persona, instructions, model reference, tools, guardrails, execution limits |
| Agent package | This Core schema | Publisher identity, immutable package version, compatibility, dependencies, interface and resources |
| Extension package | Existing Core plugin manifest | Executable component contributions and plugin dependencies |
| Installed agent | Wayang project/install model | Project-scoped configuration, connection references, secret bindings and selected package digest |
| Agent deployment | Wayang deployment / Andalus adapter | Runtime placement, identity, lifecycle, scaling and operational configuration |
| Marketplace listing | Wayang Console / marketplace service | Search metadata, reviews, verification state, visibility and commercial terms |

Do not add publisher, marketplace, artifact-store, or deployment fields to `AgentSpec`. Conversely, do not make the marketplace parse prompt text to infer package identity or dependencies.

Wayang currently has `AgentSpec`, `AgentTemplate`, `AgentPersona`, `AgentGuardrails`, and safe YAML loading in `wayang-agent`. The package schema is designed to wrap and distribute this existing definition. It does not claim that Wayang currently accepts this manifest or installs it.

## 3. Package identity and versioning

- `metadata.id` is a stable namespaced package ID, for example `acme:support-triage`.
- `metadata.version` is immutable SemVer. Publishing a changed artifact requires a new version.
- `metadata.publisher` identifies the publisher namespace; marketplace ownership/verification is resolved by the catalog, not trusted from this field alone.
- `apiVersion` versions the manifest schema independently of package SemVer.
- `agentApiRange` describes the compatible agent-definition/invocation contract.
- `runtimeCompatibility` describes the Wayang/agent runtime compatibility range.
- Tool, skill, extension and model/provider versions remain independently versioned dependencies.
- Registry metadata stores the artifact digest and signature envelope. Do not place a self-referential digest or private signing key in the manifest.

Never resolve `latest` during a production install or deployment. A project lock must pin exact package version and artifact digest, plus the resolved dependency closure.

## 4. Agent definition and invocation interface

The `spec.agent` object describes the reusable agent definition. It aligns with the current Wayang `AgentSpec` fields: local agent ID, model reference, instructions, input template, tools, step/timeout bounds, approval-required tools, output keys and structured options.

The package ID is globally namespaced; the embedded `AgentSpec.id` remains a local definition identifier for compatibility. They are not interchangeable.

The `spec.interface` object declares public JSON Schema input and output contracts. This interface is the boundary used by:
- standalone API invocation;
- chat/session adapters;
- workflow-to-agent invocation;
- agent-as-node composition;
- cross-runtime/remote invocation in a future protocol.

A parent workflow binds values to the input schema and receives a result matching the output schema. The workflow must not depend on private prompt variables or implementation-specific tool configuration.

The interface is authoritative for composition. `outputKeys` remains a convenience/documentation field on the current AgentSpec, not a replacement for an output schema.

## 5. Triggers, dependencies and configuration

### Triggers

The initial vocabulary is `chat`, `api`, `webhook`, `schedule`, and `event`. These are supported invocation styles requested by the package, not proof that a specific runtime adapter or scheduler is installed.

### Dependencies

Dependencies use namespaced package IDs, version ranges, and an explicit kind:
- `agent`
- `skill`
- `extension`
- `workflow-template`
- `solution`

The resolver must reject cycles, incompatible ranges, ambiguous identities, revoked packages, and missing mandatory dependencies. Optional dependencies may be omitted only when the agent definition remains valid without them.

Model provider/model references are capability requirements, not necessarily installable package dependencies. A future capability resolver should validate model availability and required modalities separately.

### Configuration and secrets

The package declares configuration requirements, not concrete environment values. Consumers bind:
- administrator-approved connection references;
- tenant/project-scoped secret references;
- non-secret values validated against a schema.

A package must never contain raw API keys, passwords, bearer tokens, private certificates, or tenant data. A secret reference is not itself authorization: the installer and runtime must enforce whether the agent may use it.

Tool configuration in the agent definition must use declarative connection/secret references. Marketplace scanning should reject likely embedded credentials, but scanning is defense in depth rather than the authorization boundary.

## 6. Permissions and guardrails

`requestedPermissions` is a request for review, not a grant. The target organization or tenant approves a least-privilege subset and the runtime enforces it.

Examples include scoped network destinations, secret references, storage namespaces, event topics and data resources. Wildcards must be constrained by policy.

Persona and prompt instructions guide model behavior; they are not security controls. Wayang-enforced guardrails, tool approval, connector authorization, tenant isolation and runtime policy must be enforced outside the model. The current `AgentGuardrails` implementation primarily constrains returned text; do not imply that it alone filters tool calls or provides complete data-loss prevention.

## 7. Evaluations and package resources

A package may include versioned evaluation suites and documentation/resources using relative paths. Resource paths must remain within the package root; reject absolute paths and traversal segments.

For packages that perform sensitive or consequential actions, publisher and marketplace policy may require evaluation suites before verification or publication. Evaluation results should record the exact package digest, evaluator/version, dataset or case-set identity, metrics, and timestamp. Passing an evaluation is evidence, not a guarantee of safe behavior.

## 8. Package layout

Illustrative layout:

```text
customer-support-agent/
├── wayang-agent-package.json
├── README.md
├── persona/
│   └── persona.yaml
├── evaluations/
│   └── support-agent.yaml
├── policies/
│   └── guardrails.yaml
└── assets/
    └── icon.svg
```

The manifest embeds the authoritative agent definition and references package resources by relative path. Avoid duplicated sources of truth: if a persona/policy file is referenced, define whether it is informational or authoritative. The first contract keeps the structured agent definition in the manifest; external files are resources unless a future schema explicitly defines a typed reference.

## 9. Install, compose, deploy

Keep these lifecycle objects distinct:

1. **Published package:** immutable publisher artifact with version, digest, signature/provenance and verification status.
2. **Installed agent definition:** project-scoped reference to an exact package version/digest plus local configuration bindings.
3. **Agent deployment:** running instance/service with runtime identity, operational policy, lifecycle, memory/session storage and deployment-specific resources.

Installation must show dependencies, requested permissions, configuration requirements, model requirements, license, publisher, version and digest before activation.

An installed agent can be:
- used in the Studio chat experience;
- invoked through an API or supported trigger;
- exposed as a typed agent node in another workflow;
- deployed independently and called through a versioned invocation contract.

Updates never silently rewrite installed definitions or running deployments. Provide explicit upgrade previews, compatibility checks, lockfile updates, staged rollout and rollback. Existing releases remain pinned to their original package digest.

## 10. Relationship to plugins, skills, templates and solutions

The marketplace has one catalog and artifact distribution foundation, but the artifact kinds remain distinct:

| Artifact | What it contributes |
|---|---|
| Extension/plugin | Executable components, connectors, triggers, validators, adapters and optional Studio metadata |
| Agent package | Reusable agent definition and invocation interface |
| Skill package | Reusable instruction/capability contribution with declared requirements |
| Workflow template | Importable workflow definition with typed parameters and dependencies |
| Solution package | Composition of agents, workflows, extensions, schemas, policies and dashboards |

A solution package may depend on several artifact kinds. It does not bundle credentials or override tenant security policy. A plugin can contribute an agent tool, but that does not make the plugin itself an Agent Package.

The marketplace catalog, publisher portal, artifact repository, verification pipeline, resolver/installer and governance services remain separate responsibilities. Marketplace availability is never required for an already installed runtime to execute.

## 11. Trust and compatibility

Before installation or activation, the host must verify:
- immutable artifact digest and publisher signature;
- package/schema/API and runtime compatibility;
- dependency closure and exact version constraints;
- license and policy;
- requested permissions and configuration references;
- resource path integrity and package size limits;
- evaluation requirements and revocation state.

Declarative agent packages do not require arbitrary code execution. Any executable tools/extensions they depend on follow the extension trust model and must not be loaded into Studio or the core runtime as an untrusted in-process plugin.

Community installations must support local/offline packages. Pro may produce minimal standalone or microservice runtimes. Enterprise may add private catalogs, approval policies, fleet rollout and centralized audit. These edition capabilities do not change the package format.

## 12. Compatibility and next implementation gates

1. Add schema validation and positive/negative golden fixture checks in Core CI.
2. Add a source-backed mapping from the schema's `spec.agent` fields to Wayang `AgentSpec`; preserve current AgentSpec YAML/JSON compatibility.
3. Add an agent package reader/validator in the owning host only after the Core schema is accepted and versioned.
4. Add dependency resolution and deterministic package lock entries alongside, not inside, the existing component lock.
5. Add project-scoped installation/configuration records and explicit secret/connection binding.
6. Add typed agent invocation and agent-as-node adapter tests.
7. Add publisher, signature, artifact repository, catalog and lifecycle services in their owning repositories.
8. Add evaluation execution and marketplace moderation/commercial policy as separate increments.

No marketplace install, agent-as-node runtime, package signing or hosted catalog behavior is claimed implemented by this contract document.
