# Domain Model

## Core Entities

## Book
Represents a sellable title.
- `Id` (int, unique)
- `Isbn` (string, unique)
- `Title` (string, default/source-language display name)
- `Author` (string)
- `Category` (string)
- `BasePrice` (decimal)
- `IsActive` (bool)

Book titles and other display text can be localized through the translation service. For example, a book may keep an English source title in `Title` while exposing a Vietnamese title through a `LanguageResource` entry such as `ResourceKey = book:1:title`, `LanguageCode = vn`, and `ResourceValue = Nhà Giả Kim`.

### Invariants
- `BasePrice >= 0`
- `Title` is required
- `Isbn` format validated (basic)

## InventoryItem
Tracks stock for one book.
- `BookId` (int)
- `QuantityOnHand` (int)
- `ReorderThreshold` (int)

### Invariants
- `QuantityOnHand >= 0`
- `ReorderThreshold >= 0`

## Cart
Transient selection before order creation.
- `Items: List<CartItem>`

## CartItem
- `BookId` (int)
- `Quantity` (int)
- `UnitPrice` (decimal, snapshot)

### Invariants
- `Quantity > 0`
- `UnitPrice >= 0`

## Order
Completed checkout transaction.
- `OrderId` (string/guid)
- `CreatedAtUtc` (DateTime)
- `Lines: List<OrderLine>`
- `Subtotal` (decimal)
- `DiscountTotal` (decimal)
- `GrandTotal` (decimal)

## OrderLine
- `BookId` (int)
- `Quantity` (int)
- `UnitPrice` (decimal)
- `LineDiscount` (decimal)
- `LineTotal` (decimal)

## RequestOrderDetailForm
Read-model projection for order detail screens and integrations. It is built from stored order detail lines plus catalog book metadata.
- `OrderId` (string/guid)
- `RequestedAtUtc` (DateTime)
- `Lines: List<RequestOrderDetailLine>`

## RequestOrderDetailLine
- `BookId` (int)
- `BookTitle` (string, catalog lookup, fallback `Unknown book`)
- `BookAuthor` (string, catalog lookup, fallback `Unknown author`)
- `Quantity` (int)
- `UnitPrice` (decimal, order-time snapshot)
- `LineTotal` (decimal, calculated as `Quantity * UnitPrice`)

### Read-Model Rules
- Order detail retrieval preserves the order price snapshot rather than reading current catalog price.
- Title and author are joined from catalog metadata to enrich the form.
- Missing catalog rows do not hide historical order lines; fallback metadata is returned instead.

## LanguageMaster
Catalog of supported translation languages.
- `LanguageCode` (string, unique; examples: `en`, `vn`)
- `LanguageName` (string; examples: `English`, `Vietnamese`)

### Invariants
- `LanguageCode` is required and unique.
- `LanguageName` is required.

## LanguageResource
Localized text value for a domain or UI resource.
- `LanguageCode` (string, references `LanguageMaster.LanguageCode`)
- `ResourceKey` (string; examples: `book:1:title`, `catalog:category:programming`)
- `ResourceValue` (string; translated text)

### Invariants
- `ResourceKey` is required.
- `ResourceValue` is required.
- A language cannot have duplicate `ResourceKey` values.
- `LanguageCode` must reference a supported language.

## PromotionRule
Abstract pricing rule.
- `Name`
- `Priority`
- `Apply(cartContext) -> discount adjustments`

## Domain Services
- `PricingService`: calculates totals and applies rules.
- `InventoryService`: validates and mutates stock.
- `CheckoutService`: orchestrates cart -> order.
- `TranslationService`: resolves localized values by `LanguageCode` and `ResourceKey`, falling back to source-language catalog fields when no resource exists.


## Sequence Domain Knowledge

The bookstore does need sequence domain knowledge, but it should be modeled as business ordering and identifier-generation rules rather than as a standalone `IEnumerable` demo concept.

### Identifier Sequences
- `Book.Id` and `Order.OrderId` are system-generated and must be unique.
- User input must not choose the next identifier value.
- A sequence generator or repository may allocate IDs, but the domain only depends on the resulting identity value.
- Identifier allocation must be monotonic within a single store/storage scope when integer IDs are used, but business logic must not infer creation time from `Book.Id`.

### Workflow Sequences
- Checkout follows this required order: cart validation -> active-book validation -> stock validation -> pricing calculation -> order creation -> inventory decrement -> order persistence.
- Inventory must not be decremented before all checkout validation succeeds.
- Pricing must not reserve stock.

### Ordered Collections
- Cart lines and order lines preserve insertion order for operator-facing summaries and read-model projections.
- Pricing rules are evaluated by ascending `PromotionRule.Priority`; ties are invalid unless a later rule defines an explicit tie-breaker.
- Order detail read models return lines in the same sequence stored on the order when a line sequence number is available.

### Existing Code Note
`BookIdSequence` is currently an educational enumerable for yielding integer book IDs. It is not the authoritative catalog ID allocator. `SequenceService` is the current infrastructure service for allocating numeric sequence values by key through `ISequenceRepository`; `SequenceRepository` stores and increments those values in the `Sequences` table. The service creates book/order IDs from allocated values, assigns one-based line sequence values, and orders priority-based rules deterministically.

## Repositories (Interfaces)
- `IBookRepository`
- `IInventoryRepository`
- `IOrderRepository`

## Domain Events (Future)
- `OrderPlaced`
- `InventoryDepleted`
- `LowStockDetected`

## Mapping to Existing Code
Current code includes:
- `Book` entity (minimal)
- `BookStoreEngine.CalculatePrice` (discount logic)
- `FileIO` for direct file reads

This model expands those types into a full domain foundation while keeping current logic as transitional implementation.
