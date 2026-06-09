# Domain Model

## Core Entities

## Book
Represents a sellable title.
- `Id` (int, unique)
- `Isbn` (string, unique)
- `Title` (string)
- `Author` (string)
- `Category` (string)
- `BasePrice` (decimal)
- `IsActive` (bool)

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

## PromotionRule
Abstract pricing rule.
- `Name`
- `Priority`
- `Apply(cartContext) -> discount adjustments`

## Domain Services
- `PricingService`: calculates totals and applies rules.
- `InventoryService`: validates and mutates stock.
- `CheckoutService`: orchestrates cart -> order.

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
