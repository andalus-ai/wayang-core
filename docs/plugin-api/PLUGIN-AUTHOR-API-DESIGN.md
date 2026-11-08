# Plugin author API and host SPI design

> **Status:** Proposed design, not an implemented API. This document deliberately separates the public authoring surface from Wayang runtime integration.

## Goal

Define the smallest useful Java-facing contract for plugin authors while keeping the shared Core reusable across Wayang Runtime, Wayang Console, external tooling and future non-Java workers.

The contract must not make plugin authors depend on Console, Quarkus, CDI, a database, Gamelan, Andalus, or a running Wayang service merely to describe a plugin.

## Proposed contract layers

```text
Plugin package
  ├── portable manifest (JSON Schema contract)
  ├── component descriptors (Core model)
  └── optional Java metadata provider
           |
           v
Wayang Core author API
  └── PluginDescriptorProvider -> PluginDescriptor
           |
           v
Wayang host adapter (not Core)
  ├── legacy WayangExtension / ServiceLoader adapter
  ├── metadata validation and compatibility checks
  ├── component executor binding
  ├── lifecycle and resource management
  └── placement: embedded / isolated / remote
```

Console consumes the manifest and reported inventory. It never loads the Java provider or invokes a component executor.

## Proposed minimal Java author contract

The initial candidate is a single provider interface:

```java
public interface PluginDescriptorProvider {
    PluginDescriptor descriptor();
}
```

The return type is the immutable Core model. The provider must be deterministic, side-effect-free and fast: it describes metadata; it does not start threads, open connections, read secrets, register executors or mutate a runtime.

Do not add lifecycle callbacks (`start`, `stop`, `install`, `activate`) to this author API. Those operations belong to the host SPI and differ by deployment mode.

The final package, interface name and provider registration mechanism remain open until the legacy source audit is complete.

## What is intentionally not in Core API

- `ComponentExecutor`, runtime execution context, workflow variables or node state.
- Quarkus CDI beans, Arc injection, build steps, native-image hooks.
- Registry mutation, plugin installation, classloader management and hot reload.
- Console catalog, approval, rollout, tenant or audit services.
- Remote worker transport, gRPC stubs or process launch APIs.
- Secret-bearing configuration or permission grants.

These are host or protocol concerns. A Java interface cannot represent a remote Python worker; the portable manifest and future worker contract must do that.

## Host-side adapter responsibilities

Wayang owns the implementation that:
1. Discovers providers using the explicitly configured startup mechanism.
2. Adapts legacy `WayangExtension` metadata without requiring immediate plugin rewrites.
3. Validates plugin identity, API/runtime compatibility, contribution uniqueness and requested permissions.
4. Binds exact component ID + version pairs to executor implementations.
5. Activates or rejects a contribution atomically.
6. Drains in-flight invocations before deactivation and closes plugin-owned resources.
7. Applies deployment-specific rules for embedded, isolated-local, remote and cloud-native execution.

A metadata provider is not an executor. A valid manifest is not proof that code is trusted. A Console approval is not a runtime activation.

## Compatibility rules

- Existing Wayang `ServiceLoader` providers remain supported through an adapter.
- No package rename or method signature change is part of this design document.
- Core model types remain immutable and transport-neutral.
- Plugin ID and component ID/version pairs are stable public identities; Java class names are implementation details.
- Duplicate identities fail closed; provider iteration order must not decide which implementation wins.
- Absence of execution capability metadata preserves only the documented legacy behavior; it must never imply remote or cloud support.
- Core API version, plugin package version, component version, runtime version and worker protocol version remain independent.

## Required test plan before implementation

1. API module compiles with only the Core model and JDK (no Quarkus or Console dependency).
2. A sample provider returns a stable descriptor and has no side effects on construction or descriptor retrieval.
3. Duplicate provider and duplicate component identity cases are rejected deterministically by the host adapter.
4. Existing Wayang ServiceLoader extensions still load and produce equivalent descriptors through the adapter.
5. A malformed or incompatible descriptor is rejected before any executor is activated.
6. A non-Java manifest-only plugin can be described without a Java API dependency.
7. Core API can be consumed without a Wayang runtime artifact on the classpath.

## Implementation gate

Before creating `wayang-plugin-api`, finish the exact Java source/usage inventory in [C1](../inventory/CONTRACT-INVENTORY.md). In particular, inspect existing `WayangExtension`, `Extensions`, `ComponentExecutor`, `ComponentExecutorCatalog`, `PluginManifest` and `PluginManifestValidator` implementations and all service registration files. The connector's current source lookup did not yet establish these paths, so this document does not claim source-level compatibility.
