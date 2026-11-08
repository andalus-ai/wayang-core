# ADR-0002 — Initial Core model artifact and Java baseline

- **Status:** Proposed for review
- **Date:** 2026-11-08

## Context

Wayang currently targets Java 21. The Core repository has no existing build or published artifact baseline. The shared model must be usable by Wayang, Console and non-Java tooling without inheriting Quarkus or application dependencies.

## Decision

Introduce the first artifact as `tech.kayys.wayang:wayang-core-model`, versioned independently under a `wayang-core-parent` Maven reactor. Use Java 17 as the initial source/bytecode baseline. Use records and immutable JDK collections; add no production dependencies. Use JUnit Jupiter only in test scope.

Component kinds remain validated open vocabulary tokens rather than a closed enum. Execution modes and state models are closed protocol vocabularies for this initial model and require explicit evolution review.

## Consequences

- Java 21 Wayang can consume Java 17 bytecode without requiring a second runtime baseline.
- The model stays usable outside Quarkus and does not impose framework dependencies.
- Java 17 is a proposed baseline until Console's actual build/JDK baseline and both consumer builds are inspected.
- This initial slice does not define manifest serialization, compatibility range grammar, plugin loading, schema validation, or management/worker protocols.
- Existing Wayang/Console types remain authoritative until fixture-backed migration is approved.
