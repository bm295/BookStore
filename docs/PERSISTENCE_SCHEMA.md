# Persistence Schema

## Purpose
This document defines the initial file-based persistence contract for the BookStore application. It is the source of truth for file layout, JSON schema, versioning, and corruption handling.

## Scope
- Local file storage only
- JSON document format
- Single-process write assumption
- Schema version `1`

## Storage Layout
Repositories store data under a local `data/` folder.

Initial files:
- `data/books.json`
- `data/inventory.json`
- `data/orders.json`
- `data/languages.json`
- `data/language-resources.json`

Rules:
- Writers create the `data/` directory on first write if it does not exist.
- Readers do not create files as a side effect.
- Missing files are treated as empty datasets.

## Common Envelope
Each file contains a JSON object with this envelope:

```json
{
  "schemaVersion": 1,
  "generatedAtUtc": "2026-03-26T00:00:00Z",
  "items": []
}
```

Envelope rules:
- `schemaVersion` is required.
- `generatedAtUtc` is required and stored in ISO 8601 UTC format.
- `items` is required and must be an array.
- Readers may ignore unknown properties.
- Writers emit only documented properties.

## Book Records
Stored in `data/books.json`.

```json
{
  "schemaVersion": 1,
  "generatedAtUtc": "2026-03-26T00:00:00Z",
  "items": [
    {
      "id": 1,
      "isbn": "9780131103627",
      "title": "The C Programming Language",
      "author": "Brian W. Kernighan; Dennis M. Ritchie",
      "category": "Programming",
      "basePrice": 49.99,
      "isActive": true
    }
  ]
}
```

Book rules:
- `id` is required and must be a positive integer.
- `isbn`, `title`, `author`, and `category` are required strings.
- `basePrice` is required and must be greater than or equal to zero.
- `isActive` is required.
- Duplicate `id` values are invalid.
- Duplicate `isbn` values are invalid.

## Inventory Records
Stored in `data/inventory.json`.

```json
{
  "schemaVersion": 1,
  "generatedAtUtc": "2026-03-26T00:00:00Z",
  "items": [
    {
      "bookId": 1,
      "quantityOnHand": 12,
      "reorderThreshold": 3
    }
  ]
}
```

Inventory rules:
- `bookId` is required and must reference an existing book.
- `quantityOnHand` is required and must be greater than or equal to zero.
- `reorderThreshold` is required and must be greater than or equal to zero.
- Duplicate `bookId` values are invalid.

## Language Records
Stored in `data/languages.json` for file-based storage and represented by the `LanguageMaster` table in relational storage.

```json
{
  "schemaVersion": 1,
  "generatedAtUtc": "2026-03-26T00:00:00Z",
  "items": [
    {
      "languageCode": "vn",
      "languageName": "Vietnamese"
    }
  ]
}
```

Language rules:
- `languageCode` is required and must be unique.
- `languageName` is required.
- `languageCode` uses the application's configured language code values, for example `en` for English and `vn` for Vietnamese.

## Language Resource Records
Stored in `data/language-resources.json` for file-based storage and represented by the `LanguageResource` table in relational storage.

```json
{
  "schemaVersion": 1,
  "generatedAtUtc": "2026-03-26T00:00:00Z",
  "items": [
    {
      "languageCode": "vn",
      "resourceKey": "book:1:title",
      "resourceValue": "Nhà Giả Kim"
    }
  ]
}
```

Language resource rules:
- `languageCode` is required and must reference an existing language.
- `resourceKey` is required.
- `resourceValue` is required.
- The combination of `languageCode` and `resourceKey` must be unique.
- Book title translations use the key pattern `book:{bookId}:title` so the English/source title can remain on the book while translated titles are stored as resources.

## Order Records
Stored in `data/orders.json`.

```json
{
  "schemaVersion": 1,
  "generatedAtUtc": "2026-03-26T00:00:00Z",
  "items": [
    {
      "orderId": "0d9f9824-cc7f-4e70-bf4a-a61e22d6920e",
      "createdAtUtc": "2026-03-26T00:00:00Z",
      "lines": [
        {
          "bookId": 1,
          "quantity": 2,
          "unitPrice": 49.99,
          "lineDiscount": 5.00,
          "lineTotal": 94.98
        }
      ],
      "subtotal": 99.98,
      "discountTotal": 5.00,
      "grandTotal": 94.98
    }
  ]
}
```

Order rules:
- `orderId` is required and must be unique.
- `createdAtUtc` is required and must be in UTC.
- `lines` is required and must contain at least one line.
- Each line requires `bookId`, `quantity`, `unitPrice`, `lineDiscount`, and `lineTotal`.
- `quantity` must be greater than zero.
- `unitPrice`, `lineDiscount`, and `lineTotal` must be greater than or equal to zero.
- `subtotal`, `discountTotal`, and `grandTotal` must be greater than or equal to zero.
- `grandTotal` must equal `subtotal - discountTotal`.

## Relational Order Detail Projection
The EF Core relational model used by `OrderFormRepository` represents order detail retrieval with three logical sets:

- `RequestOrderDetailForms`: one row per requested order detail form, keyed by internal `Id` with unique `OrderId`.
- `RequestOrderDetailLines`: one row per order detail line, linked to `RequestOrderDetailForms` by `RequestOrderDetailFormEntityId`; stores `BookId`, `Quantity`, and order-time `UnitPrice`.
- `CatalogBooks`: one row per catalog book metadata record, keyed by internal `Id` with unique `BookId`; stores `Title` and `Author` for read-model enrichment.
- `Sequences`: one row per named numeric sequence, keyed by internal `Id` with unique `SequenceKey`; stores `CurrentValue` as the last allocated value.

Projection rules:
- `GetRequestOrderDetailForm` first resolves the requested form by `OrderId`.
- The detail query joins `RequestOrderDetailLines` to `CatalogBooks` by `BookId` to populate title and author.
- A missing catalog row is treated as stale historical data and returns fallback metadata instead of dropping the line.
- `LineTotal` is calculated in the application projection from `Quantity * UnitPrice`; it is not stored on the relational line entity.

## Serialization Rules
- Encoding: UTF-8
- Date/time format: ISO 8601 with `Z`
- Numbers use JSON numeric literals, not strings
- Repositories read and write the whole document in v1

## Error Handling
Repository read rules:
- Missing file returns an empty dataset.
- Malformed JSON throws `MalformedDataException`.
- Unsupported `schemaVersion` throws `UnsupportedSchemaVersionException`.
- Duplicate identifiers or invariant violations in stored data throw `MalformedDataException`.

Repository write rules:
- Writers must serialize to a temporary file and replace the target file atomically when possible.
- Writers must not leave a truncated primary file after a failed write.
- Validation occurs before the final file replacement step.

## Concurrency and Migration Assumptions
- Single-process writes are assumed in v1.
- Cross-process locking is out of scope for v1.
- Future schema versions may add fields, but version `1` readers are not required to support future versions.
- Any migration from version `1` to a later version must be explicit and tested.


## Sequence Persistence Rules

- New or migrated persisted collections that have business-visible order must include an explicit sequence/position value; physical file order or database row order is not a contract.
- `Book.Id` can be generated by `SequenceService` through `ISequenceRepository` and the `Sequences` table, a file-backed sequence store, or database identity, but duplicate generated IDs are malformed data.
- `Order.OrderId` must be unique and stable after creation. If an integer or prefixed order sequence is introduced later, the sequence state must be stored separately from order records and updated atomically with order persistence.
- Order detail lines should store a line sequence number before multiple-line checkout is implemented so `GetRequestOrderDetailForm` can return the operator-facing order of lines deterministically.
- Legacy order-line data without an explicit sequence value may be read in stored array order for JSON files or primary-key order for relational rows, but new implementations should add an explicit sequence field instead of relying on incidental storage order.

## Non-Goals
- Database storage
- Incremental append-only journals
- Binary serialization
- Multi-file transactions across machines
