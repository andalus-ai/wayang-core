# Schema governance and compatibility fixtures

## Canonical sources

| Contract | Schema path | Status | Compatibility rule |
|---|---|---|---|
| Existing Wayang plugin manifest | `schemas/plugin-manifest/v1/schema.json` | Compatibility baseline, copied from Wayang runtime | Preserve its shape and validation semantics; changes require a new schema version or an explicitly compatible additive revision |
| Core component descriptor | `schemas/component-descriptor/0.1/schema.json` | Experimental proposal | Not a serialization claim about existing Wayang Java classes |

The plugin manifest schema was copied from `andalus-ai/wayang` at source blob `b20f35c69741f78edbf34ed60cddb0dee8f523e8`, originally located at `wayang-runtime/src/main/resources/schema/wayang-plugin-manifest.schema.json`. It uses JSON Schema Draft 2020-12 and the schema ID `https://wayang.ai/schemas/plugin-manifest-1.0.json`.

## Golden fixtures

- `plugin-manifest/v1/examples/valid-minimal.json` is a positive fixture using the current `apiVersion/kind/metadata/spec` layout.
- `plugin-manifest/v1/examples/invalid-version.json` is a negative fixture; the plugin SemVer has an invalid leading zero.
- `component-descriptor/0.1/examples/minimal.json` exercises the proposed Core descriptor shape.

The Maven GitHub Actions workflow validates these fixtures with `check-jsonschema`. The negative fixture is required to fail validation.

## Identifier compatibility

The existing plugin manifest accepts lowercase dotted/hyphenated identifiers such as `acme.csv-reader`. The Core `NamespacedId` accepts this legacy form and the explicit `namespace:local-name` form proposed for new contracts. The v1 plugin schema remains unchanged and continues to accept only its existing legacy form.

Do not rewrite stored plugin IDs during migration. Do not treat the new colon form as valid in the v1 manifest unless a future manifest schema version explicitly adopts it.

## Versioning and evolution

1. Never edit an existing schema in a way that silently changes validation behavior for already published manifests.
2. Additive optional fields may be considered within a compatible schema minor revision only when existing validators and consumers safely ignore them.
3. Tightening a pattern, changing required fields, changing field meaning, or removing accepted values is a breaking schema change.
4. Give every published schema an immutable versioned `$id`; do not repoint an existing immutable ID at incompatible content.
5. Keep schema version, plugin package SemVer, component SemVer, plugin API version and runtime version independent.
6. Preserve golden examples from released versions indefinitely; add new fixtures rather than rewriting history.
7. JSON Schema validation does not perform signature verification, permission authorization, dependency resolution or runtime compatibility evaluation.

## Deliberately deferred

The proposed component descriptor schema is not yet bound to a canonical JSON serialization library. Before declaring it stable, agree the Java-to-JSON mapping, required empty collections/null omission, schema reference rules, metadata extension rules, affinity semantics and cross-language canonical examples.
