# ADR 0003: Application-Owned Sequence Boundary

- Status: Accepted
- Date: 2026-08-22

## Context

Identifier allocation is initiated by application workflows but was represented by `ISequenceRepository` and `SequenceService` in the infrastructure namespace. This made the use-case policy appear to be an infrastructure concern and encouraged future application code to depend outward on persistence types. The same service also combined persisted identifier allocation with pure collection-ordering rules.

## Decision

Own the `ISequenceRepository` port, its persistence-neutral `SequenceRecord`, and `SequenceService` in `BookStore.Application.Sequences`. The EF Core `SequenceRepository` remains an infrastructure adapter and implements the application port.

Keep deterministic collection ordering in the separate application-level `SequenceOrdering` component. Identifier allocation and collection ordering can now change independently.

## Consequences

- Dependency direction is `Infrastructure.SequenceRepository -> Application.ISequenceRepository <- Application.SequenceService`.
- Application workflows can use sequence allocation without naming an infrastructure abstraction.
- Pure ordering rules no longer require constructing a persistence-oriented service.
- The move changes the namespaces of recently introduced public sequence types. Callers using the old infrastructure namespaces must update their imports.
- There are more focused files and one additional application component.

## Alternatives Considered

- Leave the port in infrastructure and rely only on dependency injection. This improves construction flexibility but does not correct dependency ownership.
- Keep all sequence operations in one service. This avoids another type but retains unrelated reasons to change.
- Introduce compatibility wrappers in the old namespaces. This would preserve imports at the cost of maintaining duplicate public contracts and obscuring the intended boundary in this early-stage library.
