package com.bookstore.application.commands;

import com.bookstore.application.ports.BookLookup;
import com.bookstore.application.ports.InventoryRepository;
import com.bookstore.domain.inventory.InventoryItem;
import java.util.Objects;

public final class AdjustStockUseCase {
    private final BookLookup books;
    private final InventoryRepository inventory;

    public AdjustStockUseCase(BookLookup books, InventoryRepository inventory) {
        this.books = Objects.requireNonNull(books);
        this.inventory = Objects.requireNonNull(inventory);
    }

    public InventoryItem execute(int bookId, int quantityDelta) {
        if (!books.exists(bookId)) {
            throw new IllegalArgumentException("Book does not exist: " + bookId);
        }
        InventoryItem current = inventory.findByBookId(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory does not exist for book: " + bookId));
        InventoryItem updated = current.adjustBy(quantityDelta);
        inventory.save(updated);
        return updated;
    }
}
