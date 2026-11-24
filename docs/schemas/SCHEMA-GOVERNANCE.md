# Schema governance and compatibility fixtures

## Canonical sources

| Contract | Schema path | Status | Compatibility rule |
|---|---|---|---|
| Existing Wayang plugin manifest | `schemas/plugin-manifest/v1/schema.json` | Compatibility baseline, copied from Wayang runtime | Preserve its shape and validation semantics; changes require a new schema version or an explicitly compatible additive revision |
| Core component descriptor | `schemas/component-descriptor/0.1/schema.json` | Experimental proposal | Not a serialization claim about existing Wayang Java classes |
| Wayang Agent Package | `schemas/agent-package/v1/schema.json` | Experimental proposed interchange contract | New package kind; does not change the legacy Plugin manifest or claim an installer exists |

The plugin manifest schema was copied from `andalus-ai/wayang` at source blob `b20f35c69741f78edbf34ed60cddb0dee8f523e8`, originally located at `wayang-runtime/src/main/resources/schema/wayang-plugin-manifest.schema.json`. It uses JSON Schema Draft 2020-12 and the schema ID `https://wayang.ai/schemas/plugin-manifest-1.0.json`.

The Agent Package schema uses JSON Schema Draft 2020-12 and the immutable schema ID `https://wayang.ai/schemas/agent-package-1.0.json`. It describes a distributable custom agent, not an executable plugin, installed project instance, marketplace listing, or deployment. Its embedded `spec.agent` is aligned with the current Wayang `AgentSpec` shape, but no consumer adapter is implemented yet.

## Golden fixtures

Positive compatibility fixtures:

- `plugin-manifest/v1/examples/valid-minimal.json` covers the smallest valid legacy manifest.
- `plugin-manifest/v1/examples/valid-with-dependency.json` covers multiple contributed components, prerelease SemVer, optional dependency declarations, and multiple permissions.
- `agent-package/v1/examples/valid-minimal.json` covers a standalone agent package with typed input/output schemas.
- `agent-package/v1/examples/valid-with-dependencies.json` covers plugin/skill dependencies, connection and secret references, requested permissions, triggers and evaluation resources.

Negative compatibility fixtures under `plugin-manifest/v1/examples/invalid/`:

- `invalid-version.json` — rejects a plugin SemVer with an invalid leading zero.
- `missing-publisher.json` — rejects a missing required publisher.
- `invalid-component-id.json` — rejects a component ID outside the legacy identifier grammar.
- `duplicate-permissions.json` — rejects duplicate permission declarations.
- `unknown-root-property.json` — rejects unknown root-level fields.

Negative agent package fixtures under `agent-package/v1/examples/invalid/`:

- `missing-interface.json` — rejects an agent package without its public invocation interface.

CI validates every positive fixture and requires every negative fixture to fail. Add a fixture whenever a compatibility edge or schema rule is changed. Do not delete or rewrite historical fixtures to make a new schema pass.

## Identifier compatibility

The existing plugin manifest accepts lowercase dotted/hyphenated identifiers such as `acme.csv-reader`. The Core `NamespacedId` accepts this legacy form and the explicit `namespace:local-name` form proposed for new contracts. The v1 plugin schema remains unchanged and continues to accept only its existing legacy form.

The Agent Package schema requires the explicit namespaced form for package and publisher IDs, for example `acme:support-triage`. The embedded Wayang agent definition keeps its existing local `AgentSpec.id` grammar. These identifiers have different ownership and must not be substituted for one another.

Do not rewrite stored plugin IDs during migration. Do not treat the new colon form as valid in the v1 legacy Plugin manifest unless a future manifest schema version explicitly adopts it.

## Versioning and evolution

1. Never edit an existing schema in a way that silently changes validation behavior for already published manifests.
2. Additive optional fields may be considered within a compatible schema minor revision only when existing validators and consumers safely ignore them.
3. Tightening a pattern, changing required fields, changing field meaning, or removing accepted values is a breaking schema change.
4. Give every published schema an immutable versioned `$id`; do not repoint an existing immutable ID at incompatible content.
5. Keep schema version, package SemVer, component SemVer, plugin API version, agent API version and runtime version independent.
6. Preserve golden examples from released versions indefinitely; add new fixtures rather than rewriting history.
7. JSON Schema validation does not perform signature verification, permission authorization, dependency resolution, evaluation, or runtime compatibility evaluation.

## Deliberately deferred

The proposed component descriptor schema is not yet bound to a canonical JSON serialization library. Before declaring it stable, agree the Java-to-JSON mapping, required empty collections/null omission, schema reference rules, metadata extension rules, affinity semantics and cross-language canonical examples.

The Agent Package schema is also experimental. Before declaring it stable, add consumer-side mapping tests against Wayang's `AgentSpec`, define package lock serialization, add dependency resolution semantics, and verify that input/output JSON Schemas can be enforced by the eventual invocation adapters.
