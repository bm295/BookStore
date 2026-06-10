# AI-Native Delivery Backlog

Use this backlog for implementation work. Pull exactly one ready slice at a time and expand it with `SLICE_DEFINITIONS.md` before coding.

## Status Notes
- `[x]` completed in the current repository
- `[ ]` not started

## Baseline Preservation
- [x] S00 Characterize current distinct-title pricing behavior in existing tests. Depends on: none.
- [x] S01 Guard invalid empty file path in existing tests. Depends on: none.
- [ ] S02 Add missing characterization tests for current pricing edge cases before refactoring. Depends on: S00.

## Wave 1: Error Model and Domain Foundation
- [x] S10 Add `BookStoreException` base type and unit tests. Depends on: none.
- [ ] S11 Add `DomainValidationException`, `DuplicateEntityException`, and `EntityNotFoundException`. Depends on: S10. (Partially started: `DomainValidationException` exists; duplicate/not-found exceptions remain.)
- [ ] S12 Add `InsufficientStockException`, `PersistenceException`, `MalformedDataException`, and `UnsupportedSchemaVersionException`. Depends on: S10.
- [ ] S13 Expand `Book` to include ISBN, title, author, category, decimal base price, and `IsActive`. Depends on: S11.
- [ ] S14 Add `Book` validation rules for required fields, non-negative price, and basic ISBN format. Depends on: S13.
- [ ] S15 Add `InventoryItem` with invariant checks for non-negative quantity and reorder threshold. Depends on: S11.
- [ ] S16 Add inventory stock adjustment behavior that rejects negative resulting stock. Depends on: S12, S15.

## Wave 2: Cart and Checkout
- [ ] S20 Add `CartItem` with quantity and unit-price invariants. Depends on: S11.
- [ ] S21 Add `Cart` aggregate that merges duplicate book additions into one logical line. Depends on: S20.
- [ ] S22 Add cart removal and quantity-decrement behavior. Depends on: S21.
- [ ] S23 Add pricing summary model with subtotal, discount total, and grand total. Depends on: S20.
- [ ] S24 Add `Order` and `OrderLine` models with total invariants. Depends on: S11, S23.
- [ ] S25 Add checkout validation for empty cart and inactive or missing books. Depends on: S13, S21, S24.
- [ ] S26 Add checkout stock validation and atomic inventory decrement behavior. Depends on: S16, S24, S25.

## Wave 3: Pricing Engine
- [ ] S30 Introduce `PricingContext` and promotion-rule abstractions without changing current totals. Depends on: S02, S23. (Initial Clean Architecture refactor introduced pricing service and discount policy boundaries; `PricingContext` remains.)
- [ ] S31 Add `DistinctTitleBundlePromotion` for 1 to 5 title bundle discounts using decimal pricing. Depends on: S30.
- [ ] S32 Add bundle optimizer that chooses the lowest total price instead of a largest-bundle-first heuristic. Depends on: S31.
- [ ] S33 Add mixed-price bundle coverage so discounts apply to actual line prices, not a single shared price. Depends on: S31.
- [ ] S34 Add non-combinability and priority behavior to the pricing engine. Depends on: S30, S31.
- [ ] S35 Add deterministic pricing trace output. Depends on: S34.
- [ ] S36 Replace direct `BookStoreEngine` discount logic with the promotion engine while preserving public behavior intentionally covered by tests. Depends on: S32, S33, S35.

## Wave 4: Persistence
- [ ] S40 Add repository interfaces for books, inventory, and orders. Depends on: S13, S15, S24.
- [ ] S41 Add JSON envelope serialization for `books.json`. Depends on: S40.
- [ ] S42 Add JSON envelope serialization for `inventory.json`. Depends on: S40.
- [ ] S43 Add JSON envelope serialization for `orders.json`. Depends on: S40, S24.
- [ ] S44 Add missing-file behavior that returns empty datasets. Depends on: S41, S42, S43.
- [ ] S45 Add malformed-data detection and typed persistence exceptions. Depends on: S12, S41, S42, S43.
- [ ] S46 Add unsupported-schema-version handling. Depends on: S12, S41, S42, S43.
- [ ] S47 Add atomic write behavior with temporary file replacement. Depends on: S41, S42, S43.

## Wave 5: Application Queries and Reporting
- [ ] S50 Add inventory snapshot query with low-stock flag. Depends on: S15, S40, S42.
- [ ] S51 Add daily sales summary query grouped by UTC day. Depends on: S43.
- [ ] S52 Add top-selling books query. Depends on: S43.
- [x] S53 Enrich request order detail form query with catalog metadata join. Depends on: existing EF Core order form repository.

## Wave 6: Developer Experience
- [ ] S60 Add `BookStoreTests/Unit` and `BookStoreTests/Integration` folder organization. Depends on: none.
- [ ] S61 Add CI workflow for restore, build, and test. Depends on: none.
- [ ] S62 Add analyzers and formatting configuration. Depends on: none.
- [x] S63 Add ADR folder and template. Depends on: none.

## Recommended Next 5 Slices
1. S11 Complete the first domain exception set.
2. S13 Expand `Book` metadata model.
3. S15 Add `InventoryItem` invariants.
4. S20 Add `CartItem` invariants.
5. S31 Add `DistinctTitleBundlePromotion` for decimal bundle discounts.


## Wave 7: Localization and Translation
- [x] S70 Document translation domain knowledge and add `LanguageMaster`/`LanguageResource` persistence tables. Depends on: existing EF Core database context.

## Wave 8: Sequence Knowledge and Deterministic Ordering
- [x] S80 Add `SequenceService` backed by `ISequenceRepository` and the `Sequences` table for generated IDs, line positions, and priority ordering. Depends on: sequence documentation.
- [ ] S81 Add cart/order line sequence preservation from `AddCartLine` through `Checkout`. Depends on: S20, S50.
- [ ] S82 Persist explicit order-line sequence values and return order details sorted by that value. Depends on: S51, S81.
- [ ] S83 Add tests for promotion priority ordering and invalid priority ties. Depends on: S30.
- [x] S84 Add a relational `Sequences` table implementation of `ISequenceRepository`. Depends on: S80.
