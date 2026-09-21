# BookStore (Java 21)

BookStore is a library-first bookstore domain/application project implemented entirely in Java.

## Build and test

```bash
mvn test
```

The code is organized into `com.bookstore.domain`, `com.bookstore.application`, and `com.bookstore.infrastructure`.

Migrated behavior includes distinct-title pricing, generated sequences, deterministic ordering, book validation, line-oriented file reading, atomic JSON book persistence, and order-detail projection with catalog fallbacks.

The former .NET/C# project and tests have been removed; Java is now the only implementation and test stack.
