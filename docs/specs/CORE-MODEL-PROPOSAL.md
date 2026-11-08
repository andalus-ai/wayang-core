# C2 — Core model contract proposal

- **Status:** Proposed; not yet a released API or implemented Java model.
- **Depends on:** C1 source inventory and compatibility fixtures.
- **Purpose:** Define the smallest host-neutral component/plugin metadata model before choosing Java package/API details.

## Design goals

1. Stable identity independent of Java class names, repository paths, UI labels or worker addresses.
2. One plugin package may contribute multiple components, connectors, triggers, providers, schemas and UI metadata.
3. A component descriptor is metadata; it does not contain executable code or runtime service objects.
4. Component/node/connector/plugin/extension/worker are separate concepts.
5. Execution placement is a runtime decision based on capabilities and policy, not an instruction embedded in the component identity.
6. Manifests are untrusted declarations. They do not grant permissions or prove publisher authenticity.
7. Metadata must be consumable by Java, Flutter, Python, Go and external tooling.

## Proposed aggregate

```text
PluginManifest
  apiVersion
  kind = "Plugin"
  metadata
    id                  # stable namespaced plugin ID
    version             # SemVer
    publisher
    license?
    displayName?
    description?
  compatibility
    pluginApiRange
    runtimeRange?
    coreContractRange?
  contributions
    components[]
    connectors[]
    triggers[]
    providers[]
    schemas[]
    uiMetadata[]
  dependencies[]
  permissions[]
  artifact
    digest?
    signatureReference?
```

The existing manifest schema is the compatibility baseline. This proposal adds fields conceptually; it does not authorize changing the existing schema without a versioned migration and fixtures.

## Component descriptor

A component is an independently versioned capability contributed by a plugin or supplied by the platform.

```text
ComponentDescriptor
  id                    # stable namespaced ID
  version               # SemVer
  kind                  # ACTION, TRIGGER, CONDITION, TRANSFORM, etc.
  displayName?
  description?
  category?
  inputs[]              # named ports and schema references
  outputs[]             # named ports and schema references
  configurationSchema  # URI or embedded schema reference
  capabilities[]
  execution
    supportedModes[]    # EMBEDDED, ISOLATED_LOCAL, REMOTE, CLOUD_NATIVE
    stateModel           # STATELESS, EXTERNALIZED, AFFINE
    payloadPortable
    cancellationSupport?
    idempotency?
    resourceHints?
  dependencies[]
  metadata              # presentation-neutral, namespaced extensions only
```

Do not assume the initial kind enumeration is final. Workflow-specific node semantics belong to Wayang; Core only defines the cross-product vocabulary needed for plugin discovery and management.

## Related types and boundaries

| Type | Definition | Owner |
|---|---|---|
| Plugin | Signed/distributable lifecycle package with dependency and contribution inventory | Core metadata; Wayang install/activate; Console catalog/governance |
| Component | Versioned executable capability contract | Core metadata; Wayang execution |
| Node | Workflow graph element configured to use a component or native workflow construct | Wayang |
| Connector | Implementation integrating an external system/protocol; may contribute one or more components | Core descriptor vocabulary; Wayang runtime implementation |
| Extension | Host integration point and contributed implementation | Minimal author API in Core; host SPI in Wayang |
| Worker | Process/container/service that advertises and hosts components | Core protocol; Wayang host adapter |
| Deployment package | Compiled workflow plus selected dependency closure and runtime profile | Wayang |
| Runtime instance | Running Wayang deployment enrolled in Console | Wayang identity agent; Console registry |

## Identity and versioning rules

- IDs use a stable reverse-domain or namespace-like convention; exact grammar must be agreed before schema v1.
- Plugin version, component version, plugin API version, Core contract version, runtime version and management protocol version are independent.
- A component ID must not be reused for a different semantic capability.
- A component version change is required when port meaning, configuration semantics, execution guarantees or compatibility changes incompatibly.
- Display names and descriptions are not identifiers.
- Worker addresses, tenant IDs, credentials and environment-specific endpoints do not belong in a portable component descriptor.
- A descriptor may reference a schema by stable ID and version; schema content must be immutable for a released version.

## Execution metadata rules

- Declared modes describe supported hosting models; they do not force the runtime to choose a mode.
- Absence of a declaration means legacy/default embedded behavior only where existing Wayang compatibility requires it; never infer remote support.
- REMOTE/CLOUD_NATIVE requires portable serialized inputs and outputs.
- AFFINE state requires an affinity declaration and explicit recovery limitations.
- Idempotency and cancellation are behavioral claims that must be tested by the provider.
- Worker health and live availability are runtime inventory, not static plugin manifest data.

## Permissions and trust

- Manifest permissions are requested capabilities, not grants.
- Host policy decides whether a plugin may access filesystem, network, secrets, databases, process execution or tenant-scoped resources.
- Publisher identity, signature verification, digest validation, vulnerability policy and revocation are separate supply-chain checks.
- Never load untrusted plugin code into the Wayang process as a sandbox.
- Console approval does not mean installed/active; track catalog approval, distribution, runtime verification and activation as separate states.

## Schema evolution

- Preserve the current JSON Schema 2020-12 manifest as a compatibility fixture.
- Use additive optional fields within a compatible schema/API minor version.
- Reject unknown required semantics; permit explicitly namespaced extension metadata where designed.
- Never repurpose a field or identifier.
- Keep schema compatibility distinct from plugin binary/API compatibility.
- Define canonical examples and invalid fixtures before declaring schema v1 stable.

## Questions to resolve before Java API implementation

- [ ] Confirm the stable ID grammar against existing component IDs and marketplace naming.
- [ ] Inspect the actual ComponentDescriptor and PluginManifest Java types and serialization annotations.
- [ ] Decide whether component kinds are an open string vocabulary or a closed enum with extension namespace.
- [ ] Decide whether ports use JSON Schema, AsyncAPI-like schemas, or a small schema-reference abstraction.
- [ ] Decide the minimum Java baseline and artifact namespace after inspecting Console build.
- [ ] Define the exact plugin API compatibility range grammar.
- [ ] Define signature and artifact provenance format without coupling to a registry vendor.
- [ ] Agree whether Core owns JSON Schema only or also protobuf source files.

## Acceptance criteria

This proposal becomes an implementable C2 contract only after each open question is resolved, current manifest/descriptor fixtures are added, and both Wayang and Console maintainers approve the ownership and compatibility rules. Until then, it is a design proposal, not a stable public API.
