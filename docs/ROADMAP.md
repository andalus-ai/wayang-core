# Wayang Core roadmap

> Status: implementation in progress. Checkboxes represent verified repository state, not intention.

## C0 — Repository and architecture foundation
- [x] Establish shared Core repository and ownership boundary.
- [x] Define dependency direction and initial contract families.
- [ ] Add contribution guide, license policy and release policy.
- [x] Propose initial Maven coordinates and Java 17 baseline in [ADR-0002](decisions/ADR-0002-core-model-java-baseline.md); pending consumer verification.

## C1 — Contract inventory and extraction plan
- [x] Start source-backed inventory of existing Wayang and Console contracts (initial, incomplete draft: [CONTRACT-INVENTORY.md](inventory/CONTRACT-INVENTORY.md)).
- [ ] Complete Java source and usage tracing.
- [ ] Add serialization characterization fixtures for existing manifest and management protocol.

## C2 — Core model and schemas
- [x] Implement initial immutable component model in `wayang-core-model`.
- [ ] Implement plugin manifest and contribution model after current Wayang serialization fixtures.
- [ ] Define compatibility range grammar and schema contracts.
- [ ] Add JSON Schema, canonical examples and golden fixtures.
- [ ] Verify model invariants against current Wayang component descriptors.

## C3 — Plugin API and SPI
- [ ] Extract minimal plugin author API.
- [ ] Separate author API from host-specific discovery SPI.
- [ ] Define lifecycle and contribution contracts.
- [ ] Preserve Wayang ServiceLoader compatibility.

## C4 — Management contracts
- [ ] Move canonical management protocol only after protobuf golden fixtures and consumer adapter inventory.

## C5 — Worker contracts
- [ ] Define language-neutral invocation/result/error contract and conformance fixtures.

## C6 — Consumer migration
- [ ] Migrate Wayang and Console only after compatibility characterization and independent build verification.

## Release gates
No stable release until API/schema review, unit and conformance tests, compatibility fixtures, Wayang and Console consumer verification, migration notes, release matrix, checksums, SBOM and provenance are complete.
