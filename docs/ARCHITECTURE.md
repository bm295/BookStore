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

## 6) Risk Areas and Mitigations
- **Risk:** Breaking existing tests during model expansion.  
  **Mitigation:** Add characterization tests before refactoring.

- **Risk:** Discount complexity growth.  
  **Mitigation:** Rule engine with ordered, independently tested rules.

- **Risk:** File format instability.  
  **Mitigation:** Versioned schema for persisted records.

## 7) Companion Documents
- Use `USE_CASES.md` for application-level boundaries and command/query definitions.
- Use `PRICING_SPEC.md` for canonical pricing behavior.
- Use `PERSISTENCE_SCHEMA.md` for file layout and JSON contracts.
- Use `ERROR_MODEL.md` for exception taxonomy.
- Use `ADR/` for stable architecture decisions.

## 8) Incremental Clean Architecture Layout
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
