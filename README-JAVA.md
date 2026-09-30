# BookStore Java

Canonical project documentation is in `README.md`. Run `mvn test` to verify the Java implementation.

## Stock adjustment

`application.commands.AdjustStockUseCase` accepts a book lookup and an inventory repository
through the interfaces in `application.ports`. Call
`execute(bookId, quantityDelta)` to save and return the updated `InventoryItem`.
The returned item exposes `quantityOnHand()` and `isLowStock()`; low stock means
the quantity is at or below its existing reorder threshold. For local file
storage, use `RepositoryBookLookup` with `BookRepository` and
`FileInventoryRepository` pointed at the data directory. Inventory is stored
in `inventory.json` using the schema in `docs/PERSISTENCE_SCHEMA.md`.
