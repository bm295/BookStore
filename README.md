# BookStore (C# 14 / .NET 10)

This repository is configured for **.NET 10** and **C# 14** (`LangVersion=preview`) and is now documented for **AI-native development**.

## Start Here
- AI agent operating guide: `AGENTS.md`
- Canonical AI markdown execution order: `docs/EXECUTION_ORDER.md`

## Documentation Hub
- `docs/PRODUCT_REQUIREMENTS.md`  
  Functional and non-functional scope, user roles, and acceptance criteria.
- `docs/DOMAIN_MODEL.md`  
  Core entities, relationships, invariants, and sequence-domain rules.
- `docs/USE_CASES.md`  
  Application use cases, command boundaries, and actor interactions.
- `docs/ARCHITECTURE.md`  
  Target architecture, component boundaries, and evolution plan.
- `docs/PRICING_SPEC.md`  
  Canonical pricing rules, money behavior, and promotion semantics.
- `docs/PERSISTENCE_SCHEMA.md`  
  File storage contract, JSON schema, and versioning rules.
- `docs/ERROR_MODEL.md`  
  Typed exception taxonomy and error-handling rules.
- `docs/AI_DEVELOPMENT_WORKFLOW.md`  
  Practical workflow for prompt-driven, test-first implementation.
- `docs/TEST_STRATEGY.md`  
  Testing pyramid, suites, conventions, and quality gates.
- `docs/BACKLOG.md`  
  Thin, AI-executable delivery slices.
- `docs/SLICE_DEFINITIONS.md`  
  Rules for turning backlog items into single-slice implementation tasks.
- `docs/EXECUTION_ORDER.md`  
  Exact markdown file reading and execution order for AI-native delivery.
- `docs/ADR/`  
  Architecture decision records and ADR template.

## Reading Order
Use `docs/EXECUTION_ORDER.md` as the canonical sequence.

Quick sequence:
1. `docs/PRODUCT_REQUIREMENTS.md`
2. `docs/DOMAIN_MODEL.md`
3. `docs/USE_CASES.md`
4. `docs/ARCHITECTURE.md`
5. `docs/ERROR_MODEL.md`
6. `docs/PRICING_SPEC.md`
7. `docs/PERSISTENCE_SCHEMA.md`
8. `docs/BACKLOG.md`
9. `docs/SLICE_DEFINITIONS.md`
10. `docs/TEST_STRATEGY.md`
11. `docs/AI_DEVELOPMENT_WORKFLOW.md`

## Operating Principle
For each new feature:
1. Pick one ready slice from `docs/BACKLOG.md`.
2. Confirm acceptance criteria from product requirements.
3. Confirm supporting contracts in `docs/USE_CASES.md`, `docs/PRICING_SPEC.md`, `docs/PERSISTENCE_SCHEMA.md`, or `docs/ERROR_MODEL.md` as needed.
4. Add tests first.
5. Implement minimal code to pass.
6. Refactor.
7. Update docs when behavior changes.

## Prerequisites

- .NET 10 SDK (preview)

## Restore and run tests

```bash
dotnet restore
dotnet test
```

## Existing IEnumerable demo: `BookIdSequence`

`BookStore/BookIdSequence.cs` demonstrates `IEnumerable<int>` by yielding a sequence of book IDs. It is an educational enumerable, not the production catalog ID allocator; `BookStore/Infrastructure/Services/SequenceService.cs` reads sequence rows by key through `ISequenceRepository` before allocating the next value in the `Sequences` table.

Run only the demo tests:

```bash
dotnet test --filter "FullyQualifiedName~BookIdSequenceTests"
```
