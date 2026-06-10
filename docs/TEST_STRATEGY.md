# Test Strategy

## Objectives
- Validate business-critical bookstore workflows.
- Prevent regressions during AI-assisted rapid iteration.
- Keep tests readable and deterministic.

## Test Pyramid

### Unit Tests (majority)
Target:
- Pricing calculations
- Promotion rule behavior
- Inventory validation
- Domain invariants

Location: `BookStoreTests/`

### Integration Tests (incremental)
Target:
- File persistence read/write cycles
- Use-case orchestration (checkout end-to-end in memory)

### End-to-End Tests (future)
Target:
- UI/API flows once interface layer exists

## Naming Convention
`MethodName_State_ExpectedBehavior`

Examples:
- `CalculatePrice_FiveDistinctBooks_AppliesTwentyFivePercentDiscount`
- `Checkout_InsufficientStock_ThrowsDomainException`

## Coverage Priorities
1. Pricing and discount engine
2. Stock decrement and validation
3. File parsing and error paths
4. Catalog mutation rules
5. Sequence-sensitive behavior: generated ID uniqueness, line-order preservation, checkout step atomicity, and pricing rule priority

## Required Test Types per Feature
For each new feature, include:
- Happy path test
- At least one edge case
- At least one validation/error case

## Reliability Rules
- No network dependency in tests.
- Avoid wall-clock assertions when possible.
- Keep tests independent (no shared mutable state).
- Prefer in-memory doubles for repositories.
- Assert order explicitly for sequence-sensitive results; do not rely on incidental collection or database ordering.
- Cover `SequenceService` with unit tests that verify it reads a sequence by key before repository allocation, stores sequence values in the `Sequences` table, uses the book/order sequence keys, preserves line order, formats order IDs, orders priorities, and rejects duplicate priorities.

## CI Gate Recommendation
- Run: `dotnet test`
- Block merge on test failures.
- Enforce formatting/analyzers when introduced.
