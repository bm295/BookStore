# Architecture Blueprint

## 1) Current State
- Core logic is implemented directly in a few classes.
- Pricing logic is hard-coded in `BookStoreEngine`.
- File I/O uses direct stream handling.

## 2) Target Architecture (Layered)

### Domain Layer
Contains pure business logic:
- Entities/value objects
- Domain services (pricing, inventory, checkout)
- Promotion rule abstractions

### Application Layer
Coordinates use cases:
- Commands/handlers (e.g., `CreateOrder`, `AdjustStock`)
- DTO mapping
- Validation orchestration

### Infrastructure Layer
External concerns:
- File persistence implementation
- (future) database adapters
- Logging and serialization

### Interface Layer (future)
- CLI / Web API / UI front-end
- Converts user requests into application commands

## 3) Design Principles
- Dependency inversion (domain independent of infrastructure).
- Small interfaces + composition.
- Explicit rule objects over `if/else` pyramids for discounts.
- Deterministic and testable domain behavior.

## 4) Proposed Project Evolution
- `BookStore/Domain/*`
- `BookStore/Application/*`
- `BookStore/Infrastructure/*`
- `BookStoreTests/Unit/*`
- `BookStoreTests/Integration/*`

(Physical folder split can be incremental without immediate project fragmentation.)

## 5) Key Technical Decisions
1. Keep monetary values as `decimal` (not `int`).
2. Introduce typed exceptions for domain validation.
3. Encapsulate pricing rules behind a promotion interface.
4. Use repository interfaces to isolate storage concerns.
5. Preserve backward compatibility where feasible during refactors.
6. Treat sequence concerns as application/domain contracts: identifier allocation belongs behind ports, workflow ordering belongs in use cases, and presentation ordering is explicit in read models.

## 6) Sequence Architecture Guidance

Sequence knowledge is required in three places and should not leak across layers:

- **Identifier generation:** define an application port such as `ISequenceGenerator` or rely on repository/database identity generation. Domain entities receive IDs but do not know how the next value is allocated.
- **Workflow ordering:** application services own ordered flows such as checkout so validation, pricing, inventory mutation, and persistence happen in a deterministic sequence.
- **Read-model ordering:** infrastructure adapters must persist and return explicit line sequence values when order-line order matters. Database row order must never be treated as meaningful without an `OrderLineSequence`/position column or equivalent sort key.

`BookStore.Application.Sequences.SequenceService` is the application service for sequence-oriented behavior: it depends on the application-owned `ISequenceRepository` port, requests a sequence record by key, and asks the adapter to allocate the next value. `BookStore.Infrastructure.Repositories.SequenceRepository` implements that port and stores numeric state in the `Sequences` table. `SequenceOrdering` separately owns the pure rules for assigning one-based line positions and ordering priority-based rules while rejecting priority ties. `BookIdSequence` can remain as a compatibility/demo enumerable, but production catalog registration should use `SequenceService` or a future application abstraction rather than deriving IDs from existing entities.

## 7) Risk Areas and Mitigations
- **Risk:** Breaking existing tests during model expansion.  
  **Mitigation:** Add characterization tests before refactoring.

- **Risk:** Discount complexity growth.  
  **Mitigation:** Rule engine with ordered, independently tested rules.

- **Risk:** File format instability.  
  **Mitigation:** Versioned schema for persisted records.

## 8) Companion Documents
- Use `USE_CASES.md` for application-level boundaries and command/query definitions.
- Use `PRICING_SPEC.md` for canonical pricing behavior.
- Use `PERSISTENCE_SCHEMA.md` for file layout and JSON contracts.
- Use `ERROR_MODEL.md` for exception taxonomy.
- Use `ADR/` for stable architecture decisions.

## 9) Incremental Clean Architecture Layout
The codebase now follows an incremental Clean Architecture layout inside the existing `BookStore` class library while preserving the public compatibility facades used by existing callers and tests.

- `BookStore/Domain/*` contains enterprise/domain concerns such as catalog models, pricing policy abstractions, discount policies, and typed domain errors.
- `BookStore/Application/*` contains use-case and application-service boundaries such as cart price calculation and file-read ports.
- `BookStore/Infrastructure/*` contains external adapters such as line-oriented file access and EF Core repositories/projections.
- Root-level `BookStoreEngine`, `Book`, and `FileIO` classes remain as compatibility facades so the refactor does not force immediate consumer changes.

Dependency direction is inward-only:

```text
Infrastructure -> Application -> Domain
Compatibility Facades -> Application/Infrastructure
Domain -> no Application or Infrastructure dependencies
```

Architecture tests in `BookStoreTests/Architecture` protect the dependency rules for the Domain and Application layers.

`OrderFormRepository.GetRequestOrderDetailFormAsync` is an infrastructure read-model adapter: it accepts an application command, resolves the requested form, and joins order detail lines with catalog book metadata in EF Core before returning application DTOs.
The infrastructure database model includes translation tables for localized catalog text. `LanguageMaster` stores supported language codes and names, while `LanguageResource` stores translated values keyed by resource identifier and language. Application/domain translation behavior should depend on abstractions, with EF Core tables remaining an infrastructure detail.
