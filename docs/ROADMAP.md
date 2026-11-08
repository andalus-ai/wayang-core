# Wayang Core roadmap

> Status: proposed. The repository is bootstrapped; phases are not complete until code, tests and consumer verification exist.

## C0 — Repository and architecture foundation
- [x] Establish shared Core repository and ownership boundary.
- [x] Define dependency direction and initial contract families.
- [ ] Add contribution guide, license policy and release policy.
- [ ] Record ADR for artifact coordinates, Java baseline and schema tooling.

## C1 — Contract inventory and extraction plan
- [x] Start source-backed inventory of existing Wayang and Console contracts (initial, incomplete draft: [CONTRACT-INVENTORY.md](inventory/CONTRACT-INVENTORY.md)).
- [ ] Inventory component/plugin/extension contracts in Wayang.
- [ ] Inventory runtime management protobuf and Console DTOs.
- [ ] Classify each type: shared contract, Wayang implementation, Console implementation, or obsolete.
- [ ] Add serialization characterization fixtures before moving public types.
- [ ] Identify semantic duplicates; do not merge types merely because names match.

## C2 — Core model and schemas
- [ ] Introduce namespaced identifiers, version types and stable component/plugin descriptors.
- [ ] Define plugin manifest and component descriptor schemas.
- [ ] Define capability vocabulary, permission model and compatibility metadata.
- [ ] Add schema validation and golden fixtures.
- [ ] Publish first explicitly experimental model/schema artifacts.

## C3 — Plugin API and SPI
- [ ] Extract a minimal plugin author API.
- [ ] Separate author API from host-specific discovery SPI.
- [ ] Define contribution inventory and lifecycle capability declarations.
- [ ] Provide a sample plugin and conformance tests.
- [ ] Preserve Wayang ServiceLoader compatibility during migration.

## C4 — Management contracts
- [ ] Move canonical runtime management protocol source into Core.
- [ ] Cover enrollment, registration, heartbeat, inventory, typed commands and acknowledgements.
- [ ] Define compatibility negotiation and additive evolution rules.
- [ ] Generate/consume server and client types from the same source.
- [ ] Keep Console persistence and application services local.

## C5 — Worker contracts
- [ ] Define language-neutral component invocation/result/error envelope.
- [ ] Specify deadline, cancellation, idempotency, duplicate delivery and payload limits.
- [ ] Define worker registration, capability negotiation, health and graceful drain.
- [ ] Add protocol conformance fixtures and testkit.

## C6 — Consumer migration
- [ ] Migrate Wayang contracts to Core with deprecated compatibility facades where necessary.
- [ ] Migrate Console management contracts to Core without importing Wayang runtime modules.
- [ ] Add cross-repository compatibility CI.
- [ ] Publish a release matrix for Core, Wayang and Console.

## C7 — SDK and ecosystem
- [ ] Java plugin SDK and testkit.
- [ ] Python/Go worker SDKs only when concrete remote worker use cases exist.
- [ ] Plugin package validator and signing/provenance tooling.
- [ ] Compatibility tooling for third-party plugin authors.

## Release gates
A Core release is not ready merely because code compiles. Require API/schema review; unit and schema conformance tests; compatibility fixtures against the previous supported release; consumer build/test verification in Wayang and Console; migration notes and compatibility matrix; artifact checksums, SBOM and provenance.
